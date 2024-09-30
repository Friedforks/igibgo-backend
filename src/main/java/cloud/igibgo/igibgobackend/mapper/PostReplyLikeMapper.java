package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostReplyLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReplyLikeMapper extends JpaRepository<PostReplyLike,Long> {
    boolean existsByPostReplyPostReplyIdAndUserUserId(Long postReplyId, Long userId);
}
