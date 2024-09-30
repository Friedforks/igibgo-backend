package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.FUser.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionMapper extends JpaRepository<Collection, Long>{
    @Query("select c from Collection c where c.fUser.userId=:userId")
    public List<Collection> findAllByUser(Long userId);
}
