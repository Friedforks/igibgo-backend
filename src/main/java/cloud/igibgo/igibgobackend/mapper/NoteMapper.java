package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteMapper extends JpaRepository<Note,String> {
    @Query("select n from NoteTag nt join nt.note n where nt.tagText = :tag")
    public List<Note> findAllByTag(String tag);

    @Query("select n from Note n where n.title like %:title%")
    public List<Note> findAllByTitle(String title);

    @Query("select distinct nt.tagText from NoteTag nt")
    public List<String> findDistinctTags();

}
