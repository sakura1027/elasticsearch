package sakura;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Article {

    private long id;

    private String title;

    private String content;

}
