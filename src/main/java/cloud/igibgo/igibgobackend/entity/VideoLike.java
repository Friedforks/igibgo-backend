package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "video_like")
public class VideoLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long videoLikeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    @JsonBackReference
    public Video video;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;
}
