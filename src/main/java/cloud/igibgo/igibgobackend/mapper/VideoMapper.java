package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Video.Video;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // search by title
    // websearch_to_tsquery('chinese', :videoTitle) is a function to convert the search string to tsquery, only postgresql 11+ support
    @Query(value = "SELECT v.* FROM video v WHERE v.title_tsv @@ websearch_to_tsquery('chinese', :videoTitle)" +
            "order by ts_rank(v.title_tsv, websearch_to_tsquery('chinese', :videoTitle)) desc",// rank by relativity
            countQuery = "SELECT COUNT(*) FROM video v WHERE v.title_tsv @@ websearch_to_tsquery('chinese', :videoTitle)",
            nativeQuery = true)
    Page<Video> searchByTitle(String videoTitle, Pageable pageable);

    @Query("select v from Video v where v.author.userId=:userId")
    List<Video> findAllByAuthorUserId(Long userId);
}
