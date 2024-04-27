package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "note_reply")
public class NoteReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long noteReplyId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    @JsonBackReference
    public Note note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
    public String replyContent;
    public LocalDateTime replyDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));

}
