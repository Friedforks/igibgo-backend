package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "note_tag")
public class NoteTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long noteTagId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    @JsonBackReference
    public Note note;
    public String tagText;

    // make Set sort by noteTagId
    @Override
    public int hashCode() {
        return noteTagId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NoteTag) {
            return noteTagId.equals(((NoteTag) obj).noteTagId);
        }
        return false;
    }
}
