package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookmarkMapper extends JpaRepository<Bookmark, Long>{
    Optional<Bookmark> findByBookmarkName(String bookmarkName);
    List<Bookmark> findAllByUserUserId(Long userId);
}
