package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMapper extends JpaRepository<Post, String > {
    @Query("select p from Post p join p.tags pt where pt.tagText=:tag")
    public List<Post> findAllByTag(String tag);

    @Query("select p from Post p join p.author pt where pt.userId=:authorId")
    List<Post> findAllByAuthorId(Long authorId);
}
