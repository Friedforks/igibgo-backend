package cloud.igibgo.igibgobackend.entity.Post;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import jakarta.persistence.*;

@Entity
@Table(name = "post_reply_like")
public class PostReplyLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long postReplyLikeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_reply_id")
    public PostReply postReply;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;
}
