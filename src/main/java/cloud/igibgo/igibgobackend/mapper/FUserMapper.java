package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.FUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface FUserMapper extends JpaRepository<FUser,Long> {
    FUser findByEmail(String email);
}
