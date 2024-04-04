package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "note_reply")
public class NoteReply {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long noteReplyId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    public Note note;
    public String replyContent;
    public LocalDateTime replyDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
}
