package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Post.PostReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostReplyMapper extends JpaRepository<PostReply, Long> {
    List<PostReply> findAllByPostPostIdAndParentReplyIsNull(String postId);

    @Query("select pr from PostReply pr where pr.parentReply.postReplyId=:postReplyId")
    List<PostReply> findAllByParentReplyPostReplyId(Long postReplyId);


    @Modifying
    @Transactional
    @Query("update PostReply pr set pr.childReplyCount = " +
            "(select count(*) from PostReply child where child.parentReply.postReplyId=:parentReplyId) " +
            "where pr.postReplyId=:parentReplyId")
    void updateParentReplyChildCount(Long parentReplyId);

    @Modifying
    @Transactional
    @Query("update PostReply pr set pr.likeCount =" +
            " (select count (*) from PostReplyLike prl where prl.postReply.postReplyId=:postReplyId ) " +
            "where pr.postReplyId=:postReplyId")
    void updateLikeCount(Long postReplyId);
}
