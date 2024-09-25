package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.FUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.beans.Transient;
import java.util.Optional;

public interface FUserMapper extends JpaRepository<FUser, Long> {
    Optional<FUser> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("update FUser f set f.password=:password where f.userId=:userId")
    void updatePasswordByUserId(Long userId, String password);

    @Modifying
    @Transactional
    @Query("update FUser f set f.username=:newUsername where f.userId=:userId")
    void updateUsernameByUserId(Long userId,String newUsername);

    @Modifying
    @Transactional
    @Query("update FUser f set f.avatarUrl=:avatarUrl where f.userId=:userId")
    void updateAvatarUrlByUserId(Long userId,String avatarUrl);
}
