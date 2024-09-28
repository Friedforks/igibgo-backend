package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "post_reply")
public class PostReply {
    @Id
    public Long postReplyId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    public Post post;
    public String replyContent;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
}
