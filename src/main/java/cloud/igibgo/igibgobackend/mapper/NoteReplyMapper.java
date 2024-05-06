package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.NoteReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteReplyMapper extends JpaRepository<NoteReply, Long> {
    public Long countByNoteNoteId(String noteId);
}
