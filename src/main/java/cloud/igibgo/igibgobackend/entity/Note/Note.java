package cloud.igibgo.igibgobackend.entity.Note;

import cloud.igibgo.igibgobackend.entity.FUser.Collection;
import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "note")
public class Note {
    @Id
    public String noteId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_id")
    public Collection collection;// fk

    public Long likeCount=0L;
    public Long saveCount=0L;
    public Long viewCount=0L;
    public Long replyCount=0L;

    public String noteUrl;
    public LocalDateTime uploadDate = LocalDateTime.now();
    public String title;

    @OneToMany(mappedBy = "note", fetch = FetchType.EAGER)
    @JsonManagedReference
    Set<NoteTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "note", fetch = FetchType.EAGER)
    @JsonManagedReference
    Set<NoteReply> replies = new HashSet<>();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Note && ((Note) obj).noteId.equals(this.noteId);
    }
}

