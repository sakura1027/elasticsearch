package sakura.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "spring_data", type = "article")
public class Article2 {

    @Id
    @Field(type = FieldType.Long, store = true)
    private long id;

    @Field(type = FieldType.text, store = true, analyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.text, store = true, analyzer = "ik_smart")
    private String content;

}
