package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String postId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
    public Long likeCount = 0L;
    public Long viewCount = 0L;
    public LocalDateTime uploadDate = LocalDateTime.now();
    public String postContent;
    public int postType;
    public String title;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PostTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PostReply> replies = new ArrayList<>();
}
