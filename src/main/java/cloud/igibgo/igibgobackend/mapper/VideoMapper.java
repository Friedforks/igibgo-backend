package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoMapper extends JpaRepository<Video, String> {

    @Query("select n from Video n join n.tags nt where nt.tagText=:tag")
    List<Video> findAllByTag(String tag);
}
