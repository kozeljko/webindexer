package si.kozelj.indexer.models.rest;

public class ResultWrapper {

    private final String documentName;
    private final Integer frequency;
    private final String snippet;

    public ResultWrapper(String documentName, Integer frequency, String snippet) {
        this.documentName = documentName;
        this.frequency = frequency;
        this.snippet = snippet;
    }

    public String getDocumentName() {
        return documentName;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public String getSnippet() {
        return snippet;
    }
}
