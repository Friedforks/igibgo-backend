package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "note_bookmark")
public class NoteBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookmarkNoteId;
    String noteId;
    long userId;
    String folder;
}

