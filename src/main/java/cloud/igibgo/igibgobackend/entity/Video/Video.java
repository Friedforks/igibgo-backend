package cloud.igibgo.igibgobackend.entity.Video;

import cloud.igibgo.igibgobackend.entity.FUser.Collection;
import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "video")
public class Video {
    @Id
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
    public LocalDateTime uploadDate = LocalDateTime.now();
    public String title;

    @JsonManagedReference
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VideoReply> replies= new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VideoTag> tags = new ArrayList<>();
}
