package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "video_tag")
public class VideoTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long videoTagId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    public Video video;
    public String tagText;
}
