package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import cloud.igibgo.igibgobackend.entity.Post.*;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.ConstantUtil;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private UploadUtil uploadUtil;

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private PostImageMapper postImageMapper;

    public String uploadPostImage(MultipartFile image, Long authorId) throws IOException {
        // Check 1: if the author exist
        Optional<FUser> userOptional = fUserMapper.findById(authorId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Author does not exist");
        }
        // Check 2: file type
        String originalFilename = image.getOriginalFilename();
        assert originalFilename != null;
        String imageSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        List<String> supportedSuffixList = List.of("png", "jpg", "jpeg", "gif", "svg", "webp", "ico", "bmp");
        if (supportedSuffixList.stream().noneMatch(suffix -> suffix.equalsIgnoreCase(imageSuffix))) {
            throw new IllegalArgumentException(
                    "Unsupported file type, only support png, jpg, jpeg, gif, svg, webp, ico, bmp");
        }
        String generatedImageId = UUID.randomUUID().toString();
        String newFileName = generatedImageId + "." + imageSuffix;
        Path tmpDir = Paths.get(ConstantUtil.tmpPath);
        // create directory if not exist
        if (!Files.exists(tmpDir)) {
            Files.createDirectories(tmpDir);
        }
        Path tmpImageFile = Files.createTempFile(tmpDir, null, newFileName);
        image.transferTo(tmpImageFile);
        // upload video cover to COS
        String image_url = uploadUtil.upload(tmpImageFile.toFile(), newFileName, "post-img/");
        PostImage postImage = new PostImage();
        postImage.postImageId = generatedImageId;
        postImage.author = userOptional.get();
        postImage.imageUrl = image_url;
        postImageMapper.save(postImage);
        return image_url;
    }

    public Page<Post> getPostsInOrder(PageRequest pageRequest) {
        return postMapper.findAll(pageRequest);
    }

    public List<Post> getPostsByTags(List<String> tags) {
        Set<Post> posts = new HashSet<>();
        for (String tag : tags) {
            posts.addAll(postMapper.findAllByTag(tag));
        }
        return posts.stream().toList();
    }

    public Post getPostByPostId(String postId) {
        Optional<Post> postOptional = postMapper.findById(postId);
        // Check 1: if the post exist
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        // 1. get the post
        Post post = postOptional.get();
        // 2. increase the view count
        post.viewCount++;
        // 3. save the post to db
        postMapper.save(post);
        return post;
    }

    @Resource
    private PostReplyMapper postReplyMapper;

    @Resource
    private PostReplyLikeMapper postReplyLikeMapper;

    public void likePostReply(Long postReplyId, String token) {
        // Check 1: validate token
        FUser user = fUserService.checkLogin(token);
        // Check 2: if the postReply exist
        Optional<PostReply> postReplyOptional = postReplyMapper.findById(postReplyId);
        if (postReplyOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        PostReply postReply = postReplyOptional.get();
        // Check 3: if the user has liked the post
        if (postReplyLikeMapper.existsByPostReplyPostReplyIdAndUserUserId(postReply.postReplyId, user.userId)) {
            return;
        }
        // 1. save the like to post_reply_like table
        PostReplyLike postReplyLike = new PostReplyLike();
        postReplyLike.postReply = postReply;
        postReplyLike.user = user;
        postReplyLikeMapper.save(postReplyLike);

        // 2. update the like count of post_reply
        postReplyMapper.updateLikeCount(postReplyId);
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private PostTagMapper postTagMapper;

    public void uploadPost(String postContent, String tags, String token, String title) {
        // Check 1: if token is valid
        String email = redisTemplate.opsForValue().get(token);
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Token is invalid, perhaps you have not logged in");
        }
        // 1. get user by email
        Optional<FUser> userOptional = fUserMapper.findByEmail(email);
        // Check 2: if the user exist
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User does not exist");
        }
        FUser user = userOptional.get();
        // 1. Generate post Id
        String generatedPostId = UUID.randomUUID().toString();
        // 2. Create a post
        Post post = new Post();
        post.postId = generatedPostId;
        post.author = user;
        post.postContent = postContent;
        post.title = title;
        // 2. Save post
        postMapper.save(post);
        // 3. Split tags into list
        String[] tagList = tags.split(",");
        // 3. Save tags
        List<PostTag> postTags = new ArrayList<>();
        for (String tag : tagList) {
            PostTag postTag = new PostTag();
            postTag.post = post;
            postTag.tagText = tag;
            postTags.add(postTag);
        }
        postTagMapper.saveAll(postTags);
    }

    public void deleteReply(Long replyId, String token) {
        // Check 1: validate token
        FUser user = fUserService.checkLogin(token);
        // Check 2: if the postReply exist
        Optional<PostReply> postReplyOptional = postReplyMapper.findById(replyId);
        if (postReplyOptional.isEmpty()) {
            throw new IllegalArgumentException("Post reply does not exist");
        }
        // Check 3: if the user is the author of the reply
        PostReply postReply = postReplyOptional.get();
        if (!postReply.user.equals(user)) {
            throw new IllegalArgumentException("You are not the author of the reply");
        }
        // 1. delete the reply
        postReplyMapper.deleteById(replyId);
        // 2. update the child reply count of parent reply
        if (postReply.parentReply != null) {
            postReplyMapper.updateParentReplyChildCount(postReply.parentReply.postReplyId);
        }
    }

    public List<Post> getPostsByAuthorId(Long authorId) {
        return postMapper.findAllByAuthorUserId(authorId);
    }

    public Page<Post> getPostsByKeywords(String keyword, PageRequest pageRequest) {
        return postMapper.findAllByTitleContainsOrPostContentContains(keyword, pageRequest);
    }

    public List<PostReply> getPrimaryReplies(String postId) {
        // Check 1: if the post exist
        Optional<Post> postOptional = postMapper.findById(postId);
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        return postReplyMapper.findAllByPostPostIdAndParentReplyIsNull(postId);
    }

    public List<PostReply> getChildReplies(Long postReplyId) {
        // Check 1: if the post reply exist
        Optional<PostReply> postReplyOptional = postReplyMapper.findById(postReplyId);
        if (postReplyOptional.isEmpty()) {
            throw new IllegalArgumentException("Post reply does not exist");
        }
        return postReplyMapper.findAllByParentReplyPostReplyId(postReplyId);
    }

    @Resource
    private FUserService fUserService;

    public void replyToPost(String postId, String replyContent, String token, Optional<Long> parentReplyIdOptional) {
        // Check 1: if the post exist
        Optional<Post> postOptional = postMapper.findById(postId);
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        // Check 2: check token
        FUser user = fUserService.checkLogin(token);
        // 1. create a reply
        PostReply postReply = new PostReply();
        postReply.post = postOptional.get();
        postReply.replyContent = replyContent;
        postReply.user = user;
        if (parentReplyIdOptional.isEmpty()) {
            postReply.parentReply = null;
            // 3. save the reply
            postReplyMapper.save(postReply);
        } else {
            Long parentReplyId = parentReplyIdOptional.get();
            // Check 3: if the parent reply exist
            Optional<PostReply> parentReplyOptional = postReplyMapper.findById(parentReplyId);
            if (parentReplyOptional.isEmpty()) {
                throw new IllegalArgumentException("Parent reply does not exist");
            }
            // 2. set parent reply
            postReply.parentReply = parentReplyOptional.get();
            // 3. save the reply
            postReplyMapper.save(postReply);
            // 4. update the child reply count of parent reply
            postReplyMapper.updateParentReplyChildCount(parentReplyId);
        }
    }

    public void deletePost(String postId, String token) {
        // Check 1: if token is valid
        String email = redisTemplate.opsForValue().get(token);
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Token is invalid, perhaps you have not logged in");
        }
        // 1. get user by email
        Optional<FUser> userOptional = fUserMapper.findByEmail(email);
        // Check 2: if the user exist
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User does not exist");
        }
        FUser user = userOptional.get();
        // Check 3: if the post exist
        Optional<Post> postOptional = postMapper.findById(postId);
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        Post post = postOptional.get();
        // Check 4: if the post author is the same as the user
        if (!post.author.equals(user)) {
            throw new IllegalArgumentException("You are not the author of the post");
        }
        // 2. delete the post
        postMapper.deleteById(postId);
    }
}
