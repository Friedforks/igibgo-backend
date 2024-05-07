package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoteBookmarkMapper extends JpaRepository<NoteBookmark,Long> {
    @Query("select count(n) from NoteBookmark n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);

    public Long countByNoteNoteId(String noteId);

    public Optional<NoteBookmark> findNoteBookmarkByNoteNoteIdAndUserUserId(String noteId, Long userId);
    public Optional<NoteBookmark> findNoteBookmarkByNoteNoteIdAndUserUserIdAndFolder(String noteId, Long userId, String folder);

    // get all bookmarked notes's folder by user and folder is not null
    public List<NoteBookmark> findAllByUserUserIdAndFolderIsNotNull(Long userId);

}
