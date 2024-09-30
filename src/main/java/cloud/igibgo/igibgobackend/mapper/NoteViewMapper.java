package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note.NoteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NoteViewMapper extends JpaRepository<NoteView,Long>{
    @Query("select count(n) from NoteView n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);

    // check if user already view the note
    @Query("select nv from NoteView nv where nv.note.noteId=:noteId and nv.user.userId=:userId")
    public Optional<NoteView> findByNoteIdAndUserId(String noteId, Long userId);

    public Long countByNoteNoteId(String noteId);
}