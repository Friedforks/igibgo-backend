package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteLikeMapper extends JpaRepository<NoteLike,Long> {
    @Query("select count(n) from NoteLike n where n.note.author.userId= :userId")
    public Long countByAuthorId(Long userId);
}
