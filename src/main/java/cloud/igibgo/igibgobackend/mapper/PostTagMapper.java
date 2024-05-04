package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagMapper extends JpaRepository<PostTag, Long> {
}
