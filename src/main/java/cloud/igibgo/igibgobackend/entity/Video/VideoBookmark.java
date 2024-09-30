package cloud.igibgo.igibgobackend.entity.Video;

import cloud.igibgo.igibgobackend.entity.FUser.Bookmark;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "video_bookmark")
public class VideoBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long videoBookmarkId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookmark_id")
    @JsonBackReference
    public Bookmark bookmark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "video_id")
    public Video video;

}
