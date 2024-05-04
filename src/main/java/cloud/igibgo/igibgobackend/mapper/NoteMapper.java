package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteMapper extends JpaRepository<Note,String> {
    // find all notes with at least one of the tags
    @Query("select distinct n from Note n join n.tags nt where nt.tagText in :tag")
    public Page<Note> findAllByTag(List<String> tag, Pageable pageable);

    @Query("select distinct n from Note n where n.title like %:title%")
    public Page<Note> findAllByTitle(String title,Pageable pageable);

    @Query("select distinct nt.tagText from NoteTag nt")
    public List<String> findDistinctTags();
}
