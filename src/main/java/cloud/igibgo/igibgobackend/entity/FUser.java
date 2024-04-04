package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "f_user")
public class FUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long userId;
    public String avatarUrl;
    public boolean isTeacher;
    public String email;
    public int subscribeCount;
    public String password;
    public String token;

    public FUser(boolean isTeacher,
                 String email, String password) {
        this.isTeacher = isTeacher;
        this.email = email;
        this.subscribeCount = 0;
        this.password = password;
    }

    public FUser() {
    }
}