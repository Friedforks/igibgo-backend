package cloud.igibgo.igibgobackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "f_user")
public class FUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long userId;
    public String username;
    public String avatarUrl;
    public boolean isTeacher;
    public String email;
    public int subscribeCount;
    public String password;
    public String token;

    public FUser(String username, String avatarUrl, boolean isTeacher, String email,String password) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.isTeacher = isTeacher;
        this.email = email;
        this.password = password;
        subscribeCount=0;
    }

    public FUser() {
    }
}