package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteLike;
import cloud.igibgo.igibgobackend.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VideoLikeMapper extends JpaRepository<VideoLike,Long> {
    @Query("select count(v) from VideoLike v where v.video.author.userId= :userId")
    Long countByAuthorId(Long userId);

    // check if user already like the video
    @Query("select vl from VideoLike vl where vl.video.videoId=:videoId and vl.user.userId=:userId")
    Optional<VideoLike> findByVideoIdAndUserId(String videoId, Long userId);

    Long countByVideoVideoId(String videoId);
}