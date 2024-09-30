package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NoteLikeMapper extends JpaRepository<NoteLike,Long> {
    @Query("select count(n) from NoteLike n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);

    // check if user already like the note
    @Query("select nl from NoteLike nl where nl.note.noteId=:noteId and nl.user.userId=:userId")
    public Optional<NoteLike> findByNoteIdAndUserId(String noteId, Long userId);

    public Long countByNoteNoteId(String noteId);
}