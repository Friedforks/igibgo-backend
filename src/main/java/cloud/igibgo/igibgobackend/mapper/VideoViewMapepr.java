package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Video.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VideoViewMapepr extends JpaRepository<VideoView,Long> {
    @Query("select vv from VideoView vv where vv.video.videoId=:videoId and vv.user.userId=:userId")
    Optional<VideoView> findByVideoIdAndUserId(String videoId,Long userId);

    @Query("select count(vv) from VideoView vv where vv.video.videoId=:videoId")
    Long countByVideoId(String videoId);

    @Query("select count(vv) from VideoView vv where vv.video.author.userId= :userId")
    Long countByAuthorId(Long userId);
}