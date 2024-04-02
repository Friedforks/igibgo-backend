package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_tag")
public class PostTag {
    @Id
    Long postTagId;
    String postId;// fk
    String tagText;
}
