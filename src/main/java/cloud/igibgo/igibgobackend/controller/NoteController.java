package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.Note.Note;
import cloud.igibgo.igibgobackend.entity.Note.NoteBookmark;
import cloud.igibgo.igibgobackend.entity.Note.NoteReply;
import cloud.igibgo.igibgobackend.entity.response.APIResponse;
import cloud.igibgo.igibgobackend.entity.response.ResponseCodes;
import cloud.igibgo.igibgobackend.mapper.NoteMapper;
import cloud.igibgo.igibgobackend.service.NoteService;
import cloud.igibgo.igibgobackend.util.StringUtil;
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
import java.util.Set;

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
    APIResponse<List<Note>> getAllNotes(Long userId) {
        try {
            List<Note> notes = noteMapper.findAllByAuthorUserId(userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, notes);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
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
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, StringUtil.toSnakeCase(orderBy)));
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getNotesByTags(tagList, pageRequest));
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/allTags")
    APIResponse<Set<String>> getAllTags() {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getAllTags());
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/title")
    APIResponse<Page<Note>> getNoteByTitle(String title, int page, int size) {
        try {
            if (title == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "title cannot be null", null);
            }
            PageRequest pageRequest = PageRequest.of(page, size);
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

    @GetMapping("/get/reply")
    APIResponse<List<NoteReply>> getAllReplies(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.getAllReplies(noteId));
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/like")
    APIResponse<Void> likeNote(String noteId, Long userId) {
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

    @GetMapping("/unlike")
    APIResponse<Void> unlikeNote(String noteId, Long userId) {
        try {
            noteService.unlikeNote(noteId, userId);
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
    APIResponse<String> replyNote(String noteId, String replyContent, Long authorId) {
        try {
            noteService.replyNote(noteId, replyContent, authorId);
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
    APIResponse<Void> deleteReply(Long replyId, String token) {
        try {
            noteService.deleteReply(replyId, token);
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
            List<String> tagList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .toList();
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

    @DeleteMapping("/delete")
    APIResponse<String> deleteNote(String token, String noteId) {
        try {
            noteService.deleteNote(token, noteId);
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

    @GetMapping("/total/reply")
    APIResponse<Long> getTotalReplies(String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.noteTotalReply(noteId));
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

    @GetMapping("/is/liked")
    APIResponse<Boolean> isLiked(String noteId, Long userId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.isLiked(noteId, userId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/is/saved")
    APIResponse<Boolean> isSaved(String noteId, Long userId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.isSaved(noteId, userId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/is/replied")
    APIResponse<Boolean> isReplied(String noteId, Long userId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteService.isReplied(noteId, userId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
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
    @PostMapping("/bookmark/new")
    APIResponse<Boolean> bookmarkNote(String noteId,
                                      Long userId,
                                      String folder) {
        try {
            List<String> folderList = List.of(folder.split(","));
            if (folder.isEmpty()) {
                folderList = List.of();
            }
            noteService.bookmarkNote(noteId, userId, folderList);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, true);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), false);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", false);
        } catch (Exception e) {
            log.error("Unhandled error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", false);
        }
    }

    @GetMapping("/bookmark/get")
    APIResponse<List<NoteBookmark>> getNoteBookmarksByUserIdAndNoteId(Long userId, String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null,
                    noteService.getNoteBookmarksByUserIdAndNoteId(userId, noteId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
