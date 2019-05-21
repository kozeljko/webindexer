package si.kozelj.indexer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import si.kozelj.indexer.models.rest.ResultWrapper;
import si.kozelj.indexer.models.rest.SearchRequest;
import si.kozelj.indexer.services.FileImporter;
import si.kozelj.indexer.services.SearchEngine;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@RestController
public class IndexerRestController {

    @Autowired
    private FileImporter fileImporter;

    @Autowired
    private SearchEngine searchEngine;

    @GetMapping("/loadFiles")
    public String loadFiles() {
        fileImporter.importFiles();
        return "loading";
    }

    @PostMapping("/search")
    public String search(@RequestBody SearchRequest searchRequest) {
        long startMs = System.currentTimeMillis();
        List<ResultWrapper> queryResults;
        try {
            queryResults = searchEngine.getQueryResults(searchRequest.getQuery());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return formatResults(searchRequest.getQuery(), queryResults, System.currentTimeMillis() - startMs);
    }

    @PostMapping("/search-slowly")
    public String searchSlowly(@RequestBody SearchRequest searchRequest) {
        long startMs = System.currentTimeMillis();
        List<ResultWrapper> queryResults;
        try {
            queryResults = searchEngine.getQueryResultsSlowly(searchRequest.getQuery());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return formatResults(searchRequest.getQuery(), queryResults, System.currentTimeMillis() - startMs);
    }

    private String formatResults(String query, List<ResultWrapper> queryResults, long time) {
        String outputFormat = "Results for a query: \"" + query + "\"\n\n\tResults found in " + time + "ms\n\n";
        outputFormat += String.format("\t%-12s%-42s%-95s\n", "Frequencies", "Document", "Snippet");
        outputFormat += String.format("\t%-12s%-42s%-95s\n", "----------- ", "----------------------------------------- ", "-----------------------------------------------------------");

        queryResults.sort(Comparator.comparing(ResultWrapper::getFrequency).reversed());
        for (ResultWrapper resultWrapper : queryResults) {
            String snippet = resultWrapper.getSnippet();
            if (snippet.length() < 150) {
                outputFormat += String.format("\t%-12s%-42s%-150s\n", resultWrapper.getFrequency(), resultWrapper.getDocumentName(), snippet);
            } else {
                outputFormat += String.format("\t%-12s%-42s%-150s\n", resultWrapper.getFrequency(), resultWrapper.getDocumentName(), snippet.substring(0,150));
                for (int i = 150; i < snippet.length() - 1; i += 150) {
                    if (i > snippet.length() - 150) {
                        outputFormat += String.format("\t%-54s%-150s\n", "", snippet.substring(i));
                        break;
                    }
                    outputFormat += String.format("\t%-54s%-150s\n", "", snippet.substring(i, i + 150));
                }
            }
        }

        return outputFormat;
    }
}
