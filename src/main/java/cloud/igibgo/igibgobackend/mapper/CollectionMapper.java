package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionMapper extends JpaRepository<Collection, Long>{
}
