package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.Post.PostReply;
import cloud.igibgo.igibgobackend.entity.response.APIResponse;
import cloud.igibgo.igibgobackend.entity.Post.Post;
import cloud.igibgo.igibgobackend.entity.response.ResponseCodes;
import cloud.igibgo.igibgobackend.service.PostService;
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
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/forum")
public class PostController {
    @Resource
    private PostService postService;

    /**
     * upload post image
     *
     * @param image    image file
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

    @GetMapping("/get/allTags")
    APIResponse<Set<String>> getAllTags() {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, postService.getAllTags());
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }


    @GetMapping("/get/postId")
    APIResponse<Post> getPostById(String postId,String token) {
        try {
            // Check 1: postId cannot be null
            if (postId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postId cannot be null", null);
            }
            // 1. get post with replies and tags
            Post post = postService.getPostByPostId(postId,token);
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

    @GetMapping("/get/keyword")
    APIResponse<Page<Post>> getPostsByKeywords(String keyword,int page,int size){
        try{
            // create page request
            PageRequest pageRequest=PageRequest.of(page,size);
            return new APIResponse<>(ResponseCodes.SUCCESS,null,postService.getPostsByKeywords(keyword,pageRequest));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @PostMapping("/like/reply")
    APIResponse<Void> likePostReply(Long postReplyId, String token) {
        try {
            // 1. like the post
            postService.likePostReply(postReplyId,token);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    /**
     * get the primary replies of a post
     * @param postId post id
     * @return list of primary replies
     */
    @GetMapping("/reply/primary")
    APIResponse<List<PostReply>> getPrimaryReplies(String postId){
        try{
            // Check 1: postId cannot be null
            if (postId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postId cannot be null", null);
            }
            return new APIResponse<>(ResponseCodes.SUCCESS,null,postService.getPrimaryReplies(postId));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    /**
     * get the child replies of a post
     * @param postReplyId post reply id
     * @return list of child replies
     */
    @GetMapping("/reply/child")
    APIResponse<List<PostReply>> getChildReplies(Long postReplyId){
        try{
            // Check 1: postReplyId cannot be null
            if (postReplyId == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postReplyId cannot be null", null);
            }
            return new APIResponse<>(ResponseCodes.SUCCESS,null,postService.getChildReplies(postReplyId));
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @PostMapping("/reply/new")
    APIResponse<Void> replyToPost(String postId, String replyContent, String token, Optional<Long> parentReplyId) {
        try {
            // Check 1: postId, replyContent, authorId cannot be null
            if (postId == null || replyContent == null || token == null) {
                return new APIResponse<>(ResponseCodes.BAD_REQUEST, "postId, replyContent, authorId cannot be null", null);
            }
            // 1. reply to the post
            postService.replyToPost(postId, replyContent, token, parentReplyId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }


    // forum author side

    // forum author side
    @PostMapping("/upload")
    APIResponse<Void> uploadPost(String postContent, String tags, String token, String title) {
        try {
            List<String> tagList= Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .toList();
            postService.uploadPost(postContent, tagList, token, title);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @PostMapping("/edit")
    APIResponse<Void> editPost(String postId,String token, String tags, String title, String postContent){
        try{
            postService.editPost(postId,token,tags,title,postContent);
            return new APIResponse<>(ResponseCodes.SUCCESS,null,null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    // delete
    @DeleteMapping("/delete")
    APIResponse<Void> deletePost(String postId, String token) {
        try {
            // 1. delete the post
            postService.deletePost(postId, token);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @DeleteMapping("/reply/delete")
    APIResponse<Void> deleteReply(Long postReplyId,String token){
        try{
            // 1. delete the reply
            postService.deleteReply(postReplyId,token);
            return new APIResponse<>(ResponseCodes.SUCCESS,null,null);
        } catch (DataAccessException e) {
            log.error("Database query error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("Internal server error: " + e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
