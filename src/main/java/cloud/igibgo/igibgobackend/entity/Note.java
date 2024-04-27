package cloud.igibgo.igibgobackend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "note")
public class Note {
    @Id
    public String noteId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author")
    public FUser author;
    public Long likeCount = 0L;
    public Long saveCount = 0L;
    public Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collection_id")
    public Collection collection;// fk

    public String noteUrl;
    public LocalDateTime uploadDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    public String title;

    @OneToMany(mappedBy = "note", fetch = FetchType.EAGER)
    @JsonManagedReference
    Set<NoteTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "note", fetch = FetchType.EAGER)
    @JsonManagedReference
    Set<NoteReply> replies = new HashSet<>();


}

