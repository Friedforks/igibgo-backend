package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoteReplyMapper extends JpaRepository<NoteReply, Long> {
    public Long countByNoteNoteId(String noteId);
    public List<NoteReply> findNoteRepliesByNoteNoteIdAndAuthorUserId(String noteId, Long userId);
    public List<NoteReply> findAllByNoteNoteIdOrderByReplyDateDesc(String noteId);
}
