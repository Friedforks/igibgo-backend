package cloud.igibgo.igibgobackend.entity.Note;

import cloud.igibgo.igibgobackend.entity.FUser.Bookmark;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "note_bookmark")
public class NoteBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long noteBookmarkId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bookmark_id")
    @JsonBackReference
    public Bookmark bookmark;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    public Note note;
}

