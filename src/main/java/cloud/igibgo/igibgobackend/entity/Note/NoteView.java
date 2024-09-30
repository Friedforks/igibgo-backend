package cloud.igibgo.igibgobackend.entity.Note;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name="note_view")
public class NoteView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long note_view_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    @JsonBackReference
    public Note note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public FUser user;
}
