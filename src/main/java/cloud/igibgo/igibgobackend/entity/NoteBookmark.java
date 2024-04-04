package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "note_bookmark")
public class NoteBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookmarkNoteId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    Note note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    FUser author;

    String folder;
}

