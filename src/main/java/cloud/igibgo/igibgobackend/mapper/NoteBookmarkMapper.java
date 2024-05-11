package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoteBookmarkMapper extends JpaRepository<NoteBookmark, Long> {
    @Query("select count(n) from NoteBookmark n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);

    public Long countByNoteNoteId(String noteId);

    public List<NoteBookmark> findNoteBookmarksByNoteNoteIdAndUserUserId(String noteId, Long userId);

    // get all bookmarked notes's folder by user and folder is not null
    public List<NoteBookmark> findAllByUserUserIdAndFolderIsNotNull(Long userId);

    @Transactional
    @Modifying
    public void deleteNoteBookmarksByNoteNoteIdAndUserUserId(String noteId, Long userId);

    public List<NoteBookmark> findAllByUserUserIdAndNoteNoteId(Long userId, String noteId);
}
