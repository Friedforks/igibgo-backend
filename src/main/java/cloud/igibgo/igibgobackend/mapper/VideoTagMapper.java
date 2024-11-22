package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Video.VideoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoTagMapper extends JpaRepository<VideoTag, Long>{
    @Query("select vt.tagText from VideoTag vt")
    public List<String> findAllTags();
}
