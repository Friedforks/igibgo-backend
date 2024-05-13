package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Bookmark;
import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.service.NoteBookmarkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bookmark")
public class NoteBookmarkController {

    @Resource
    private NoteBookmarkService noteBookmarkService;

    /**
     * bookmark note
     *
     * @param noteId note to be saved
     * @param userId user who saves the note
     * @return no response
     */
    @PostMapping("/new")
    APIResponse<Boolean> bookmarkNote(String noteId,
                                      Long userId,
                                      String folder) {
        try {
            List<String> folderList = List.of(folder.split(","));
            if(folder.isEmpty()){
                folderList = List.of();
            }
            noteBookmarkService.bookmarkNote(noteId, userId, folderList);
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

    @GetMapping("/get/by/userId")
    APIResponse<List<Bookmark>> getBookmarksByUserId(Long userId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteBookmarkService.getBookmarksByUserId(userId));
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

    @GetMapping("/get/note/page")
    APIResponse<List<NoteBookmark>> getBookmarksByUserIdAndNoteId(Long userId, String noteId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, noteBookmarkService.getBookmarksByUserIdAndNoteId(userId, noteId));
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
