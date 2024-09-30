package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagMapper extends JpaRepository<PostTag, Long> {
}
