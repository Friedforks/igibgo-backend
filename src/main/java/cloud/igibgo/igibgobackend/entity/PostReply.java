package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_reply")
public class PostReply {
    @Id
    Long postReplyId;
    String postId;
    String replyContent;
    Long author;
}
