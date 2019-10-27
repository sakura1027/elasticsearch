package sakura;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;

public class ElasticSearchClientTest {

    private TransportClient client;

    @Before
    public void init() throws Exception {
        // 创建settings配置对象
        Settings settings = Settings.builder()
                .put("cluster.name", "my-elasticsearch")
                .build();
        // 创建客户端Client对象
        client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9301))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9302))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9303));
    }

    @Test
    public void createIndex() {
        client.admin().indices().prepareCreate("index_hello").get();
        client.close();
    }

    @Test
    public void setMappings() throws Exception {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("article")
                .startObject("properties")
                .startObject("id")
                .field("type", "long")
                .field("store", true)
                .endObject()
                .startObject("title")
                .field("type", "text")
                .field("store", true)
                .field("analyzer", "ik_smart")
                .endObject()
                .startObject("content")
                .field("type", "text")
                .field("store", true)
                .field("analyzer", "ik_smart")
                .endObject()
                .endObject()
                .endObject()
                .endObject();
        client.admin().indices().preparePutMapping("index_hello")
                .setType("article").setSource(xContentBuilder).get();
        client.close();
    }

    @Test
    public void addDocument() throws Exception {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder()
                .startObject()
                .field("id", 1L)
                .field("title", "sakura")
                .field("content", "1027")
                .endObject();
        client.prepareIndex("index_hello", "article", "1")
                .setSource(xContentBuilder).get();
        client.close();
    }

    @Test
    public void addDocument2() throws Exception {
        for (int i = 2; i < 101; i++) {
            Article article = Article.builder().id(i).title("胡歌_" + i).content("刘亦菲_" + i).build();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(article);
            client.prepareIndex("index_hello", "article", String.valueOf(i))
                    .setSource(jsonDocument, XContentType.JSON).get();
        }
        client.close();
    }

    @Test
    public void searchById() {
        search(QueryBuilders.idsQuery().addIds("1", "2"), false);
    }

    @Test
    public void searchByTerm() {
        search(QueryBuilders.termQuery("title", "sakura"), true);
    }

    @Test
    public void searchByQueryString() {
        search(QueryBuilders.queryStringQuery("胡歌 sakura").defaultField("title"), true);
    }

    private void search(QueryBuilder queryBuilder, boolean needHighlight) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        if (needHighlight) { // 查询结果高亮显示
            highlightBuilder.field("title").preTags("<b>").postTags("</b>");
        }

        SearchResponse searchResponse = client.prepareSearch("index_hello")
                .setTypes("article").setQuery(queryBuilder)
                .setFrom(0).setSize(5) // 分页
                .highlighter(needHighlight ? highlightBuilder : null).get();
        SearchHits searchHits = searchResponse.getHits();

        System.out.println("查询结果总记录数：" + searchHits.getTotalHits());
        System.out.println("------------------------------------------");
        Iterator<SearchHit> iterator = searchHits.iterator();
        while (iterator.hasNext()) {
            SearchHit searchHit = iterator.next();
            System.out.println(searchHit.getSourceAsString());
            Map<String, Object> document = searchHit.getSource();
            System.out.println(document.get("id"));
            System.out.println(document.get("title"));
            System.out.println(document.get("content"));

            if (needHighlight) {
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                System.out.println(highlightFields.get("title").getFragments()[0]);
            }
            System.out.println("------------------------------------------");
        }
        client.close();
    }

}
