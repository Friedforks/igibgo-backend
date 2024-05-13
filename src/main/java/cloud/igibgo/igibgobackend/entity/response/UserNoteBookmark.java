package cloud.igibgo.igibgobackend.entity.response;

import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import jakarta.persistence.Entity;
import lombok.Data;

import java.util.List;

@Data
public class UserNoteBookmark {
    public List<NoteBookmark> noteBookmarkList;
    public String folder;

    public UserNoteBookmark(List<NoteBookmark> noteBookmarkList, String folder) {
        this.noteBookmarkList = noteBookmarkList;
        this.folder = folder;
    }
}
