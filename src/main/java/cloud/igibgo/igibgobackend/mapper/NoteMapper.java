package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteMapper extends JpaRepository<Note,String> {
    @Query("select n from Note n join n.tags nt where nt.tagText=:tag")
    List<Note> findAllByTag(String tag);

    @Query("select n from Note n join fetch n.replies nr where n.noteId = :noteId")
    Note findByNoteIdWithRepliesAndTags(String noteId);

}
