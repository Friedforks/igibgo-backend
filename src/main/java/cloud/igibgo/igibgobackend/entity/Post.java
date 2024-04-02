package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "post")
public class Post {
    @Id
    Long postId;
    Long author;// fk
    Long likeCount=0L;
    Long viewCount=0L;
    LocalDateTime uploadDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    String postContent;
    int postType;
    String title;
}
