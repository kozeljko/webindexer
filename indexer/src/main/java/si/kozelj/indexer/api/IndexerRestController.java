package si.kozelj.indexer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import si.kozelj.indexer.services.FileImporter;

@RestController
public class IndexerRestController {

    @Autowired
    private FileImporter fileImporter;

    @GetMapping("/loadFiles")
    public String loadFiles() {
        fileImporter.importFiles();
        return "loading";
    }
}
