package si.kozelj.indexer.models;

import javax.persistence.*;

@Entity
public class Posting {

    @EmbeddedId
    private PostingKey postingKey;
    private Long frequency;
    private String indexes;

    public Posting() {
        postingKey = new PostingKey();
    }

    public String getWord() {
        return postingKey.getWord();
    }

    public void setWord(String word) {
        postingKey.setWord(word);
    }

    public String getDocumentName() {
        return postingKey.getDocumentName();
    }

    public void setDocumentName(String documentName) {
        postingKey.setDocumentName(documentName);
    }

    public PostingKey getPostingKey() {
        return postingKey;
    }

    public void setPostingKey(PostingKey postingKey) {
        this.postingKey = postingKey;
    }

    @Column(name = "frequency")
    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    @Column(name = "indexes")
    public String getIndexes() {
        return indexes;
    }

    public void setIndexes(String indexes) {
        this.indexes = indexes;
    }
}
