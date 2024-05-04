package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteBookmarkMapper extends JpaRepository<NoteBookmark,Long> {
    @Query("select count(n) from NoteBookmark n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);
}
