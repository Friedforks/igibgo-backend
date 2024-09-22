package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookmarkMapper extends JpaRepository<Bookmark, Long>{
    Optional<Bookmark> findByBookmarkName(String bookmarkName);
    
    @Query("select b from Bookmark b where b.user.userId=:userId")
    List<Bookmark> findAllByUserUserId(Long userId);
}
