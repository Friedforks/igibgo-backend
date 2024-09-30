package cloud.igibgo.igibgobackend.entity.Post;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import jakarta.persistence.*;

@Entity
@Table(name="post_images")
public class PostImage {
    @Id
    public String postImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public FUser author;
    public String imageUrl;
}
