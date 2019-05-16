package si.kozelj.indexer.services;

import one.util.streamex.StreamEx;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

@Service
public class FileImporter {

    private final String DATA_PATH = "D:/Libraries/Documents/Projects/webindexer/data";
    private final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.html");

    private final Logger logger = LoggerFactory.getLogger(FileImporter.class);

    public void importFiles() {
        List<Path> filePaths;
        try (Stream<Path> paths = Files.walk(Paths.get(DATA_PATH))){
            filePaths = StreamEx.of(paths).filter(pathMatcher::matches).toList();
        } catch (IOException e) {
            logger.error("Error while trying to fetch and filter file paths", e);
            return;
        }

        logger.info("Found " + filePaths.size() + " file paths to load.");

        // iterate over all files and load them
        for (Path path : filePaths) {
            parsePath(path);
            break;
        }
    }

    private void parsePath(Path path) {
        String documentText;
        try {
            String content = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
            documentText = Jsoup.parse(content).text();
            System.out.println(documentText);
        } catch (IOException e) {
            logger.error("Error while trying to load path content", e);
            return;
        }

        // TODO tokenize, normalize, stopwords
    }
}
