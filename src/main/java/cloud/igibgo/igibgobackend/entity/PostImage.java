package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name="post_images")
public class PostImage {
    @Id
    public String postImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public FUser fUser;
    public String imageUrl;
}
