package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMapper extends JpaRepository<Post, String> {
    @Query("select p from Post p join p.tags pt where pt.tagText=:tag")
    public List<Post> findAllByTag(String tag);

    List<Post> findAllByAuthorUserId(Long authorId);


    @Query(value = "SELECT p.* FROM post p WHERE " +
            "p.title_tsv @@ websearch_to_tsquery('chinese', :searchTerm) OR " +
            "p.post_content_tsv @@ websearch_to_tsquery('chinese', :searchTerm) " +
            "ORDER BY GREATEST(ts_rank(p.title_tsv, websearch_to_tsquery('chinese', :searchTerm)), " +
            "ts_rank(p.post_content_tsv, websearch_to_tsquery('chinese', :searchTerm))) DESC",
            countQuery = "SELECT COUNT(*) FROM post p WHERE " +
                    "p.title_tsv @@ websearch_to_tsquery('chinese', :searchTerm) OR " +
                    "p.post_content_tsv @@ websearch_to_tsquery('chinese', :searchTerm)",
            nativeQuery = true)
    Page<Post> findAllByTitleContainsOrPostContentContains(String searchTerm, Pageable pageable);
}
