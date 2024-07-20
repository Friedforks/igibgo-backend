package cloud.igibgo.igibgobackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="video_view")
public class VideoView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long video_view_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    @JsonBackReference
    public Video video;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;
}
