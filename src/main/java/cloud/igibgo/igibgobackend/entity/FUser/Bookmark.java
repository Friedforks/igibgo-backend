package cloud.igibgo.igibgobackend.entity.FUser;

import cloud.igibgo.igibgobackend.entity.Note.NoteBookmark;
import cloud.igibgo.igibgobackend.entity.Video.VideoBookmark;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "bookmark")
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long bookmarkId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;

    @OneToMany(mappedBy = "bookmark", fetch = FetchType.EAGER)
    @JsonManagedReference
    public List<NoteBookmark> noteBookmarks;

    @OneToMany(mappedBy = "bookmark", fetch = FetchType.EAGER)
    @JsonManagedReference
    public List<VideoBookmark> videoBookmarks;

    public String bookmarkName;

    // override equal: only compare bookmarkName
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Bookmark bookmark = (Bookmark) obj;
        return bookmarkName.equals(bookmark.bookmarkName);
    }
}
