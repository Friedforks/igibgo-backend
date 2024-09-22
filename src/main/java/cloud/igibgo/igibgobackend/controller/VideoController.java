package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.service.VideoService;
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
import org.springframework.web.bind.annotation.GetMapping;


@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {
    @Resource
    private VideoService videoService;

    // video viewer side
    /**
     * get videos in order
     *
     * @param page      page number
     * @param size      page size
     * @param orderBy   order by which field
     * @param ascending (boolean) ascending or descending
     * @return page of videos
     */
    @GetMapping("/get/order")
    APIResponse<Page<Video>> getVideosInOrder(int page, int size, String orderBy, boolean ascending) {
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
            // 3. get videos in order
            Page<Video> videos = videoService.getVideosInOrder(pageRequest);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videos);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    /**
     * get all videos by user id, used in user page
     * @param userId author userId
     * @return list of videos
     */
    @GetMapping("/get/by/userId")
    public APIResponse<List<Video>> getAllVideosByUserId(Long userId) {
        try{
            List<Video> videos = videoService.getAllVideosByUserId(userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videos);
        }
        catch(Exception e){
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }
    

    /**
     * get videos by tags
     *
     * @param tags list of tags
     * @return list of videos that have at least one of the tags
     */
    @GetMapping("/get/tags")
    APIResponse<List<Video>> getVideosByTags(List<String> tags) {
        try {
            // Check 1: tags cannot be null
            if (tags == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "tags cannot be null", null);
            }
            // 1. get videos by tags
            List<Video> videos = videoService.getVideosByTags(tags);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videos);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/videoId")
    APIResponse<Video> getVideoWithRepliesTags(String videoId,Long userId) {
        try {
            if(videoId == null){
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "videoId cannot be null", null);
            }
            Video video= videoService.getVideoByVideoId(videoId,userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, video);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/title")
    APIResponse<Page<Video>> getVideosByVideoTitle(String videoTitle,int page, int size){
        try{
            // Check 1: videoTitle cannot be null
            if (videoTitle == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "videoTitle cannot be null", null);
            }
            // 1. create a page request
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Video> videos=videoService.getVideosByVideoTitle(videoTitle,pageRequest);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videos);
        }catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @PostMapping("/like/videoId")
    APIResponse<Void> likeVideo(String videoId,Long userId) {
        try {
            videoService.likeVideo(videoId,userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @PostMapping("/unlike/videoId")
    APIResponse<Void> unlikeVideo(String videoId,Long userId){
        try {
            videoService.unlikeVideo(videoId,userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/is/liked")
    APIResponse<Boolean> isLiked(String videoId,Long userId){
        try {
            Boolean isLiked = videoService.isLiked(videoId,userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, isLiked);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/is/saved")
    APIResponse<Boolean> isSaved(String videoId,Long userId){
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videoService.isSaved(videoId,userId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
    @PostMapping("/reply")
    APIResponse<Void> replyVideo(String videoId, String replyContent, Long authorId) {
        try {
            videoService.replyVideo(videoId, replyContent, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @DeleteMapping("/delete/reply")
    APIResponse<Void> deleteReply(Long replyId, Long authorId) {
        try {
            videoService.deleteReply(replyId, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // video manager side

    /**
     * upload video
     * @param video video file (mp4, mov, flv)
     * @param videoCover video cover file (jpg, png)
     * @param authorId author
     * @param collectionId collection
     * @param title title
     * @return no response
     */
    @PostMapping("/upload")
    APIResponse<String> uploadVideo(MultipartFile video,
                                    MultipartFile videoCover,
                                    Long authorId,
                                    Long collectionId,
                                    String title,
                                    String tags) {
        try {
            // 1. upload video
            List<String> tagList = Arrays.asList(tags.split(","));
            videoService.uploadVideo(video, videoCover, authorId, collectionId, title,tagList);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
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
    APIResponse<String> deleteVideo(Long authorId,String videoId) {
        try {
            videoService.deleteVideo(authorId, videoId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
        catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }


    @GetMapping("/bookmark/get")
    APIResponse<List<VideoBookmark>> getVideoBookmarksByUserIdAndVideoId(Long userId, String videoId){
        try{
            return new APIResponse<>(ResponseCodes.SUCCESS, null, videoService.getVideoBookmarksByUserIdAndVideoId(userId, videoId));
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
    /** bookmark video
     * @param videoId video to be saved
     * @param userId user who saves the video
     * @return no response
     */
    @PostMapping("/bookmark/new")
    APIResponse<Boolean> bookmarkVideo(String videoId,
                                       Long userId,
                                       String folder) {
        try {
            List<String> folderList = List.of(folder.split(","));
            if(folder.isEmpty()){
                folderList = List.of();
            }
            videoService.bookmarkVideo(videoId, userId, folderList);
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
}
