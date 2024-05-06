package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.mapper.NoteMapper;
import cloud.igibgo.igibgobackend.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/note")
public class NoteController {
    @Resource
    private NoteService noteService;

    @Resource
    private NoteMapper noteMapper;

    // note viewer side

    @GetMapping("/get/all")
    APIResponse<List<Note>> getAllNotes() {
        return new APIResponse<>(ResponseCodes.SUCCESS, null, noteMapper.findAll());
    }

    /**
     * get notes in order
     *
     * @param page      page number
     * @param size      page size
     * @param orderBy   order by which field
     * @param ascending (boolean) ascending or descending
     * @return page of notes
     */
    @GetMapping("/get/order")
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
    @GetMapping("/get/tags")
    APIResponse<Page<Note>> getNoteByTags(String tags, int page, int size, String orderBy, boolean ascending) {
        try {
            List<String> tagList = Arrays.asList(tags.split(","));
            Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy));
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getNotesByTags(tagList, pageRequest));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/allTags")
    APIResponse<List<String>> getAllTags() {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getAllTags());
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/title")
    APIResponse<Page<Note>> getNoteByTitle(String title, int page, int size, String orderBy, boolean ascending) {
        try {
            if (title == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "title cannot be null", null);
            }
            Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy));
            Page<Note> notes = noteService.getNotesByTitle(title, pageRequest);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, notes);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }


    @GetMapping("/get/noteId")
    APIResponse<Note> getNoteWithRepliesTags(String noteId, Long userId) {
        try {
            if (noteId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "noteId cannot be null", null);
            }
            Note note = noteService.getNoteByNoteId(noteId, userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, note);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/like/videoId")
    APIResponse<String> likeNote(String noteId, Long userId) {
        try {
            noteService.likeNote(noteId, userId);
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

    @GetMapping("/reply")
    APIResponse<String> replyNote(String noteId, String content, Long authorId) {
        try {
            noteService.replyNote(noteId, content, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, "Replied to note");
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

    @GetMapping("/delete/reply")
    APIResponse<Void> deleteReply(Long replyId, Long authorId) {
        try {
            noteService.deleteReply(replyId, authorId);
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
     * bookmark note
     *
     * @param noteId note to be saved
     * @param userId user who saves the note
     * @return no response
     */

    @GetMapping("/bookmark")
    APIResponse<String> bookmarkNote(String noteId,
                                     Long userId,
                                     @RequestParam(name = "folder", required = false, defaultValue = "default") String folder) {
        try {
            noteService.bookmarkNote(noteId, userId, folder);
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
     * @param note     note
     * @param authorId author
     */
    @PostMapping("/upload")
    APIResponse<Void> uploadNote(MultipartFile note, Long authorId, Long collectionId, String title, String tags) {
        try {
            List<String> tagList = List.of(tags.split(","));
            noteService.uploadNote(note, authorId, collectionId, title, tagList);
            return new APIResponse<>(ResponseCodes.SUCCESS, "Note uploaded", null);
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

    @GetMapping("/delete")
    APIResponse<String> deleteNote(Long authorId, String noteId) {
        try {
            noteService.deleteNote(authorId, noteId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, "Note deleted");
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

    @GetMapping("/total/like")
    APIResponse<Long> getTotalLikes(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.noteTotalLike(noteId));
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

    @GetMapping("/total/view")
    APIResponse<Long> getTotalViews(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.noteTotalView(noteId));
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

    @GetMapping("/total/save")
    APIResponse<Long> getTotalSave(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.noteTotalSave(noteId));
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
}
