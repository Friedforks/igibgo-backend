package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/note")
public class NoteController {
    @Resource
    private NoteService noteService;

    // note viewer side

    /**
     * get notes in order
     * @param page page number
     * @param size page size
     * @param orderBy order by which field
     * @param ascending (boolean) ascending or descending
     * @return page of notes
     */
    @GetMapping("/notes/order")
    APIResponse<Page<Note>> getNotesInOrder(int page, int size, String orderBy, boolean ascending) {
        try {
            // Check 1: orderBy cannot be null
            if (orderBy == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "orderBy cannot be null", null);
            }

            // 1. set the direction of sorting
            Sort.Direction direction;
            if (ascending) {
                direction = Sort.Direction.ASC;
            } else {
                direction = Sort.Direction.DESC;
            }
            // 2. create a page request
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy));
            // 3. get notes in order
            Page<Note> notes = noteService.getNotesInOrder(pageRequest);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, notes);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }


    /**
     * get notes by tags
     *
     * @param tags list of tags
     * @return list of notes that have at least one of the tags
     */
    @GetMapping("/notes/tags")
    APIResponse<List<Note>> getNoteByTags(List<String> tags) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getNoteByTags(tags));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/note")
    APIResponse<Note> getNoteWithRepliesTags(String noteId) {
        try {
            Note note = noteService.getNoteWithReplies(noteId);
            if (note == null) {
                return new APIResponse<>(ResponseCodes.NOT_FOUND, "Note not found", null);
            }
            return new APIResponse<>(ResponseCodes.SUCCESS, null, note);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/note/like")
    APIResponse<String> likeNote(String noteId) {
        try {
            noteService.likeNote(noteId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    /**
     * get note by note id
     *
     * @param noteId note id
     * @return note
     */
    @GetMapping("/note/noteId")
    APIResponse<Note> getNoteByNoteId(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getNoteByNoteId(noteId));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    /**
     * save the note to user-save-note
     *
     * @param noteId note to be saved
     * @param userId user who saves the note
     * @return no response
     */
    @GetMapping("/note/bookmark")
    APIResponse<String> bookmarkNote(String noteId, Long userId) {
        try {
            noteService.bookmarkNote(noteId, userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // note manager side

    /**
     * upload note
     *
     * @param note   note
     * @param author author
     * @return response
     */
    @GetMapping("/note/upload")
    APIResponse<String> uploadNote(MultipartFile note, Long author, Long collectionId, String title) {
        try {
            noteService.uploadNote(note, author, collectionId, title);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, "Note uploaded");
        } catch (DataAccessException e) {
            log.error("Database query error", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (IOException e) {
            log.error("IO error in copying note to temporary local file", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "IO error", null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument", e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Unhandled error", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
