package top.nomelin.cometpan.pojo;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    // 公告ID
    private Integer id;
    // 公告标题
    private String title;
    // 公告内容
    private String content;
    // 发布时间
    private Date time;
    // 发布人ID
    @JsonAlias({"userid", "user_id"})
    private Integer userId;
}
