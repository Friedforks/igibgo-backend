package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewMapper extends JpaRepository<PostView,Long> {
    Long countPostViewsByPostPostId(String postId);

    boolean existsByPostPostIdAndUserUserId(String postId, Long userId);
}
