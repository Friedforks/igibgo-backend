package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "video_reply")
public class VideoReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long videoReplyId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    @JsonBackReference
    public Video video;
    public String replyContent;
    public LocalDateTime replyDate = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
}
