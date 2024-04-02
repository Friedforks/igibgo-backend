package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "note_tag")
public class NoteTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long noteTagId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    Note note;// fk
    String tagText;
}
