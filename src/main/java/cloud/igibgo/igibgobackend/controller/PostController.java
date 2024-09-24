package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Post;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.service.PostService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/forum")
public class PostController {
    @Resource
    private PostService postService;

    /**
     * upload post image
     * @param image   image file
     * @param authorId author id
     * @return image url
     */
    @PostMapping("/image/upload")
    public APIResponse<String> uploadPostImage(MultipartFile image, Long authorId) {
        try {
            String img_url = postService.uploadPostImage(image, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, img_url);
        } catch (IOException e) {
            log.error("Error in uploading post image: ", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    // forum viewer side

    /**
     * get posts in order
     *
     * @param page      page number
     * @param size      page size
     * @param orderBy   order by which field
     * @param ascending (boolean) ascending or descending
     * @return page of posts
     */
    @GetMapping("/get/order")
    APIResponse<Page<Post>> getPostsInOrder(int page, int size, String orderBy, boolean ascending) {
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
            // 3. get posts in order
            Page<Post> posts = postService.getPostsInOrder(pageRequest);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, posts);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        }
    }

    @GetMapping("/get/tags")
    APIResponse<List<Post>> getPostsByTags(List<String> tags) {
        try {
            // Check 1: tags cannot be null
            if (tags == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "tags cannot be null", null);
            }
            // 1. get posts by tags
            List<Post> posts = postService.getPostsByTags(tags);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, posts);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/postId")
    APIResponse<Post> getPostById(String postId) {
        try {
            // Check 1: postId cannot be null
            if (postId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postId cannot be null", null);
            }
            // 1. get post with replies and tags
            Post post = postService.getPostByPostId(postId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, post);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/get/authorId")
    APIResponse<List<Post>> getPostsByAuthorId(Long authorId) {
        try {
            // CHeck 1: authorId cannot be null
            if (authorId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "author id cannot be null", null);
            }
            return new APIResponse<>(ResponseCodes.SUCCESS, null, postService.getPostsByAuthorId(authorId));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/like")
    APIResponse<Void> likePost(String postId) {
        try {
            // Check 1: postId cannot be null
            if (postId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postId cannot be null", null);
            }
            // 1. like the post
            postService.likePost(postId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @GetMapping("/reply")
    APIResponse<Void> replyPost(String postId, String replyContent, Long authorId) {
        try {
            // 1. reply the post
            postService.replyPost(postId, replyContent, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // forum author side
    @GetMapping("/delete/reply")
    APIResponse<Void> deleteReply(String replyId, Long authorId) {
        try {
            // 1. delete the reply
            postService.deleteReply(replyId, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // forum author side
    @GetMapping("/upload")
    APIResponse<Void> uploadPost(String postContent, List<String> tags, Long authorId, String title) {
        try {
            // 1. upload the post
            postService.uploadPost(postContent, tags, authorId, title);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // delete
    @GetMapping("/delete")
    APIResponse<Void> deletePost(String postId, Long authorId) {
        try {
            // 1. delete the post
            postService.deletePost(postId, authorId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
