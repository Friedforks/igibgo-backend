package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostViewMapper extends JpaRepository<PostView,Long> {
    Long countPostViewsByPostPostId(String postId);

    boolean existsByPostPostIdAndUserUserId(String postId, Long userId);

    @Query("select count(pv) from PostView pv where pv.post.author.userId= :userId")
    Long countByAuthorId(Long userId);
}
