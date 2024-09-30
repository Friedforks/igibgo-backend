package cloud.igibgo.igibgobackend.entity.Post;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** sql
 * create table post_reply
 * (
 *     post_reply_id bigserial not null primary key,
 *     post_id       text      not null references post (post_id) on delete cascade,
 *     parent_reply_id bigint references post_reply (post_reply_id) on delete cascade,
 *     reply_content text      not null,
 *     like_count    int       not null default 0,
 *     reply_date   timestamp not null,
 *     user_id        int       not null references f_user (user_id) on delete cascade,
 *     reply_content_tsv tsvector generated always as ( to_tsvector('chinese', reply_content) ) stored
 * );
 * create index reply_content_tsv_idx on post_reply using gin (reply_content_tsv);
 */

@Entity
@Table(name = "post_reply")
public class PostReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long postReplyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    public Post post;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_reply_id")
    public PostReply parentReply;

    public Integer childReplyCount=0;

    public String replyContent;
    public Long likeCount=0L;
    public LocalDateTime replyDate=LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;

    // make Set sort by postReplyId
    @Override
    public int hashCode() {
        return postReplyId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PostReply) {
            return postReplyId.equals(((PostReply) obj).postReplyId);
        }
        return false;
    }
}