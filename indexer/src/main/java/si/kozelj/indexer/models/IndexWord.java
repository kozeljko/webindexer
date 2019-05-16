package si.kozelj.indexer.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class IndexWord {

    private String word;

    public IndexWord() {
    }

    public IndexWord(String word) {
        this.word = word;
    }

    @Id
    @Column(name = "word")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
