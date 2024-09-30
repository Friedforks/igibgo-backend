package cloud.igibgo.igibgobackend.mapper;

import cloud.igibgo.igibgobackend.entity.Note.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteTagMapper extends JpaRepository<NoteTag,Long> {
}
