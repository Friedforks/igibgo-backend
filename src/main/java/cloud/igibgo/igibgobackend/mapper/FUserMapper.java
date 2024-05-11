package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.FUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface FUserMapper extends JpaRepository<FUser,Long> {
    Optional<FUser> findByEmail(String email);
}
