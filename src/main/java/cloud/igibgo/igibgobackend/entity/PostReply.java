package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_reply")
public class PostReply {
    @Id
public     Long postReplyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
public     Post post;
public     String replyContent;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
public     FUser author;
}
