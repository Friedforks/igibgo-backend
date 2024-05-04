package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteViewMapper extends JpaRepository<NoteView,Long>{
    @Query("select count(n) from NoteView n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);
}