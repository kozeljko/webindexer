package si.kozelj.indexer.repositories;

import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import si.kozelj.indexer.models.Posting;
import si.kozelj.indexer.models.PostingKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PostingRepository extends JpaRepository<Posting, PostingKey> {

    @Query("select p from Posting p where p.postingKey.word=?1 and p.postingKey.documentName=?2")
    List<Posting> findByKeys(String word, String documentName);

    @Query("select p.postingKey.documentName from Posting p where p.postingKey.word in (?1) group by p.postingKey.documentName order by sum(p.frequency) desc")
    List<String> findMostFrequentPages(Collection<String> words, Pageable pageable);

    @Query("select p from Posting p where p.postingKey.documentName in (?1) and p.postingKey.word in (?2)")
    List<Posting> findTargetPostings(Collection<String> documentNames, Collection<String> words);

    default List<Posting> findTopFiveMatchingDocuments(Collection<String> words) {
        Collection<String> targetDocuments = findMostFrequentPages(words, PageRequest.of(0, 5));
        if (targetDocuments.isEmpty()) {
            return ImmutableList.of();
        }

        return findTargetPostings(targetDocuments, words);
    }
}
