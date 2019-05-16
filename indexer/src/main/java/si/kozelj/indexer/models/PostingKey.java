package si.kozelj.indexer.models;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostingKey implements Serializable {
    private String word;
    private String documentName;

    @Column(name = "word")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Column(name = "document_name")
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostingKey)) return false;
        PostingKey that = (PostingKey) o;
        return Objects.equals(getWord(), that.getWord()) &&
                Objects.equals(getDocumentName(), that.getDocumentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWord(), getDocumentName());
    }
}
