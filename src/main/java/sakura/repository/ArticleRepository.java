package sakura.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import sakura.entity.Article2;

public interface ArticleRepository extends ElasticsearchRepository<Article2, Long> {

    List<Article2> findByTitle(String title);

    List<Article2> findByTitleOrContent(String title, String content);

    List<Article2> findByTitleOrContent(String title, String content, Pageable pageable);

}
