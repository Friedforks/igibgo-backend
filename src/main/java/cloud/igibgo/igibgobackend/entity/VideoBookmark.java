package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "video_bookmark")
public class VideoBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookmarkVideoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    Video video;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    FUser user;
    String folder;
}
