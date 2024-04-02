package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "video_reply")
public class VideoReply {
    @Id
    Long videoReplyId;
    String videoId;
    String replyContent;
    LocalDateTime replyDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    Long author;
}
