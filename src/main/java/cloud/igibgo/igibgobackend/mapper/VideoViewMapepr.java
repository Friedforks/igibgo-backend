package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VideoViewMapepr extends JpaRepository<VideoView,Long> {
    @Query("select vv from VideoView vv where vv.video.videoId=:videoId and vv.user.userId=:userId")
    Optional<VideoView> findByVideoIdAndUserId(String videoId,Long userId);

    @Query("select count(vv) from VideoView vv where vv.video.videoId=:videoId")
    Long countByVideoId(String videoId);
}