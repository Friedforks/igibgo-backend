package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "note")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String noteId;
    public Long author;// fk
    public Long likeCount = 0L;
    public Long saveCount = 0L;
    public Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    public Collection collection;// fk

    public String noteUrl;
    public LocalDateTime uploadDate = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
    public String title;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<NoteReply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<NoteTag> tags = new ArrayList<>();

}

