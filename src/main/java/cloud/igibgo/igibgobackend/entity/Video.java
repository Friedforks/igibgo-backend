package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Entity
@Table(name = "video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String videoId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
    public Long likeCount = 0L;
    public Long viewCount = 0L;
    public Long saveCount = 0L;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_id")
    public Collection collection;
    public String videoUrl;
    public String videoCoverUrl;
    public LocalDateTime uploadDate = LocalDateTime.now(TimeZone.getTimeZone("Asia/Shanghai").toZoneId());
    public String title;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VideoReply> replies= new ArrayList<>();

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VideoTag> tags = new ArrayList<>();
}
