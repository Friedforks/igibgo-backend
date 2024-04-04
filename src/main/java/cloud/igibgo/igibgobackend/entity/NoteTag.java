package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "note_tag")
public class NoteTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long noteTagId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id")
    public Note note;// fk
    public String tagText;
}
