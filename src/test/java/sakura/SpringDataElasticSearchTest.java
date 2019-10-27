package sakura;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sakura.entity.Article2;
import sakura.repository.ArticleRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class SpringDataElasticSearchTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void createIndex() {
        template.createIndex(Article2.class);
    }

    @Test
    public void addDocument() {
        for (int i = 1; i < 101; i++) {
            Article2 article2 = Article2.builder()
                    .id(i).title("胡歌_" + i).content("刘亦菲_" + i).build();
            articleRepository.save(article2);
        }
    }

    @Test
    public void deleteDocumentById() {
        articleRepository.deleteById(1L);
    }

    @Test
    public void findAll() {
        Iterable<Article2> article2s = articleRepository.findAll();
        article2s.forEach(System.out::println);
    }

    @Test
    public void findById() {
        Optional<Article2> optional = articleRepository.findById(2L);
        System.out.println(optional.get());
    }

    //--------------------------- 自定义查询 ---------------------------//
    @Test
    public void findByTitle() {
        List<Article2> article2s = articleRepository.findByTitle("胡歌_2");
        article2s.forEach(System.out::println);
    }

    @Test
    public void findByTitleOrContent() {
        List<Article2> article2s = articleRepository.findByTitleOrContent("胡歌_2", "刘亦菲_3");
        article2s.forEach(System.out::println);
    }

    @Test
    public void findByTitleOrContent2() {
        Pageable pageable = PageRequest.of(0, 1);
        List<Article2> article2s = articleRepository
                .findByTitleOrContent("胡歌_2", "刘亦菲_3", pageable);
        article2s.forEach(System.out::println);
    }

    //--------------------------- 原生查询 ---------------------------//
    @Test
    public void nativeSearchQuery() {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.queryStringQuery("胡胡").defaultField("title"))
                .withPageable(PageRequest.of(0, 5)).build();
        List<Article2> article2s = template.queryForList(query, Article2.class);
        article2s.forEach(System.out::println);
    }

}
