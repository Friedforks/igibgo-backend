package cloud.igibgo.igibgobackend.entity.Post;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class Post {
    @Id
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

    @JsonManagedReference
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PostTag> tags = new ArrayList<>();
}