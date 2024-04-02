package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "note_reply")
public class NoteReply {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long noteReplyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    Note note;
    String replyContent;
    LocalDateTime replyDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    Long author;// fk
}
