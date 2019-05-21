package si.kozelj.indexer.services;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Resources;
import one.util.streamex.StreamEx;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.kozelj.indexer.models.IndexWord;
import si.kozelj.indexer.models.Posting;
import si.kozelj.indexer.repositories.IndexWordRepository;
import si.kozelj.indexer.repositories.PostingRepository;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Service
public class FileImporter {

    @Autowired
    private IndexWordRepository indexWordRepository;

    @Autowired
    private PostingRepository postingRepository;

    // TODO make it configurable
    private final String DATA_PATH = "D:/Libraries/Documents/Projects/webindexer/data";
    private final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.html");

    private final Logger logger = LoggerFactory.getLogger(FileImporter.class);

    public void importFiles() {
        List<Path> filePaths;
        Set<String> stopWords;
        try {
            filePaths = getFilePaths();
            stopWords = getStopWords();
        } catch (IOException e) {
            logger.error("Error while trying to fetch and filter file paths", e);
            return;
        }

        logger.info("Found " + filePaths.size() + " file paths to load.");

        // iterate over all files and load them
        for (Path path : filePaths) {
            parsePath(path, stopWords);
        }
    }

    public Map<String, String> getContent(Collection<String> documentNames) throws IOException {
        List<Path> filePaths = getFilePaths();
        Map<String, String> map = Maps.newHashMap();

        for (Path path : filePaths) {
            String pathName = getDocumentName(path);

            if (documentNames.contains(pathName)) {
                map.put(pathName, extractDocumentText(path));
            }
        }

        return map;
    }

    public String extractDocumentText(Path filePath) throws IOException {
        String content = new String(Files.readAllBytes(filePath), Charset.forName("UTF-8"));
        return Jsoup.parse(content).text();
    }

    public List<Path> getFilePaths() throws IOException {
        Stream<Path> paths = Files.walk(Paths.get(DATA_PATH));
        return StreamEx.of(paths).filter(pathMatcher::matches).toList();
    }

    private void parsePath(Path path, Set<String> stopWords) {
        // retrieve document text
        String documentText;
        try {
            documentText = extractDocumentText(path);
            System.out.println(documentText);
        } catch (IOException e) {
            logger.error("Error while trying to load path content", e);
            return;
        }

        // tokenize
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(documentText);
        Multimap<String, Integer> multiMap = HashMultimap.create();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];

            // normalize
            token = token.toLowerCase();

            // check if stopword
            if (stopWords.contains(token)) {
                continue;
            }

            multiMap.put(token, i);
        }

        // create document name
        String documentName = getDocumentName(path);

        // save new index words
        Collection<String> existingWords = StreamEx.of(indexWordRepository.findAllById(multiMap.keySet())).map(IndexWord::getWord).toList();
        List<IndexWord> newIndexWords = new ArrayList<>();
        for (String wordKey : multiMap.keySet()) {
            // save index word, if it hasn't been saved before
            if (!existingWords.contains(wordKey)) {
                newIndexWords.add(new IndexWord(wordKey));
            }
        }

        indexWordRepository.saveAll(newIndexWords);
        indexWordRepository.flush();

        // save new postings
        List<Posting> newPostings = new ArrayList<>();
        for (String wordKey : multiMap.keySet()) {
            Collection<Integer> indices = multiMap.get(wordKey);
            String indicesString = StreamEx.of(indices).sorted().joining(",");

            Posting newPosting = new Posting();
            newPosting.setFrequency(indices.size());
            newPosting.setDocumentName(documentName);
            newPosting.setIndexes(indicesString);
            newPosting.setWord(wordKey);

            newPostings.add(newPosting);
        }

        postingRepository.saveAll(newPostings);
        postingRepository.flush();
    }

    public String getDocumentName(Path path) {
        int nameCount = path.getNameCount();
        return path.getName(nameCount - 2) + "/" + path.getName(nameCount - 1);
    }

    // loads the stop words from the resources folder
    private Set<String> getStopWords() throws IOException {
        URL url = Resources.getResource("sl.txt");
        String text = Resources.toString(url, Charsets.UTF_8);

        return StreamEx.split(text, ",").toSet();
    }
}
