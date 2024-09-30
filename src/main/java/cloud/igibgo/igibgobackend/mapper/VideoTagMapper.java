package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Video.VideoTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoTagMapper extends JpaRepository<VideoTag, Long>{
}
