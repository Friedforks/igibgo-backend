package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NoteBookmarkMapper extends JpaRepository<NoteBookmark, Long> {
    Optional<NoteBookmark> findNoteBookmarkByNoteNoteIdAndBookmarkBookmarkId(String noteId, Long bookmarkId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteAllByBookmarkUserUserIdAndNoteNoteId(Long userId, String noteId);

    List<NoteBookmark> findAllByNoteNoteIdAndBookmarkUserUserId(String noteId, Long userId);

    Long countByNoteNoteId(String noteId);

    Long countByBookmarkUserUserId(Long userId);

    @Query("select count(nb) from NoteBookmark nb where nb.note.author.userId= :userId")
    Long countByAuthorId(Long userId);
    
}
