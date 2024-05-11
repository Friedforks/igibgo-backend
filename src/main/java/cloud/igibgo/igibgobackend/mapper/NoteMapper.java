package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteMapper extends JpaRepository<Note, String> {
    // find all notes with at least one of the tags
    @Query("select distinct n from Note n join n.tags nt where nt.tagText in :tag")
    public Page<Note> findAllByTag(List<String> tag, Pageable pageable);

    @Query("select distinct n from Note n where n.title like %:title%")
    public Page<Note> findAllByTitle(String title, Pageable pageable);

    @Query("select distinct nt.tagText from NoteTag nt")
    public List<String> findDistinctTags();

    // update like count by note id
    @Transactional
    @Modifying
    @Query("update Note n set n.likeCount = :likeCount where n.noteId = :noteId")
    public void updateLikeCountByNoteId(String noteId, Long likeCount);

    // update save count by note id
    @Transactional
    @Modifying
    @Query("update Note n set n.saveCount = :saveCount where n.noteId = :noteId")
    public void updateSaveCountByNoteId(String noteId, Long saveCount);

    // update view count by note id
    @Transactional
    @Modifying
    @Query("update Note n set n.viewCount = :viewCount where n.noteId = :noteId")
    public void updateViewCountByNoteId(String noteId, Long viewCount);

    // update reply count by note id
    @Transactional
    @Modifying
    @Query("update Note n set n.replyCount = :replyCount where n.noteId = :noteId")
    public void updateReplyCountByNoteId(String noteId, Long replyCount);

    // find all by user
    public List<Note> findAllByAuthorUserId(Long userId);
}
