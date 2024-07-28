package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Video;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoMapper extends JpaRepository<Video, String> {

    @Query("select n from Video n join n.tags nt where nt.tagText=:tag")
    List<Video> findAllByTag(String tag);

    @Transactional
    @Modifying
    @Query("update Video v set v.viewCount=:viewCount where v.videoId=:videoId")
    void updateViewCountByVideoId(String videoId, Long viewCount);

    @Transactional
    @Modifying
    @Query("update Video v set v.likeCount=:likeCount where v.videoId=:videoId")
    void updateLikeCountByVideoId(String videoId, Long likeCount);

    @Transactional
    @Modifying
    @Query("update Video v set v.saveCount=:saveCount where v.videoId=:videoId")
    void updateSaveCountByVideoId(String videoId, Long saveCount);
}
