package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "video_tag")
public class VideoTag {
    @Id
    Long videoTagId;
    String videoId;// fk
    String tagText;
}
