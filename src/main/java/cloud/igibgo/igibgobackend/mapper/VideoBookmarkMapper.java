package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.VideoBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface VideoBookmarkMapper extends JpaRepository<VideoBookmark,Long> {
    List<VideoBookmark> findAllByVideoVideoIdAndBookmarkUserUserId(String videoId, Long userId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByVideoVideoIdAndBookmarkUserUserId(String videoId, Long userId);

    Long countByVideoVideoId(String videoId);

    @Query("select count(vb) from VideoBookmark vb where vb.video.author.userId= :userId")
    Long countByAuthorId(Long userId);
}
