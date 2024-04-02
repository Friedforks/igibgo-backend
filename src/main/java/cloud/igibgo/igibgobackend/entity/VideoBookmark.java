package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "f_user_save_video")
public class VideoBookmark {
    @Id
    Long bookmarkVideoId;
    String videoId;
    long author;
    String folder;
}
