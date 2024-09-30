package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageMapper extends JpaRepository<PostImage,String> {

}
