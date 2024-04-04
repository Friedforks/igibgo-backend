package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteMapper extends JpaRepository<Note,String> {
    @Query("select n from Note n join n.tags nt where nt.tagText=:tag")
    public List<Note> findAllByTag(String tag);
}
