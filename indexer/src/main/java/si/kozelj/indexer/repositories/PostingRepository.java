package si.kozelj.indexer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.kozelj.indexer.models.Posting;
import si.kozelj.indexer.models.PostingKey;

import java.util.List;

public interface PostingRepository extends JpaRepository<Posting, PostingKey> {

    @Query("select p from Posting p where p.postingKey.word=?1 and p.postingKey.documentName=?2")
    List<Posting> findByKeys(String word, String documentName);
}
