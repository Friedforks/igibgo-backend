package cloud.igibgo.igibgobackend.entity.Post;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "post_tag")
public class PostTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long postTagId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    public Post post;
    public String tagText;
}
