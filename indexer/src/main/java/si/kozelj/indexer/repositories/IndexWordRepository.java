package si.kozelj.indexer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import si.kozelj.indexer.models.IndexWord;

public interface IndexWordRepository extends JpaRepository<IndexWord, String> {
}
