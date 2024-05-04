package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.mapper.FUserMapper;
import cloud.igibgo.igibgobackend.mapper.PostMapper;
import cloud.igibgo.igibgobackend.mapper.PostReplyMapper;
import cloud.igibgo.igibgobackend.mapper.PostTagMapper;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostService {

    @Resource
    private PostMapper postMapper;

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
        Post post=postOptional.get();
        // 2. increase the view count
        post.viewCount++;
        // 3. save the post to db
        postMapper.save(post);
        return post;
    }

    public void likePost(String postId) {
        Optional<Post> postOptional = postMapper.findById(postId);
        // Check 1: if the post exist
        if (postOptional.isEmpty()) {
            throw new IllegalArgumentException("Post does not exist");
        }
        // 1. get the post
        Post post=postOptional.get();
        // 2. increase the like count
        post.likeCount++;
        // 3. save the post to db
        postMapper.save(post);
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private PostTagMapper postTagMapper;

    public void uploadPost(String postContent, List<String> tags, Long authorId, String title) {
        // Check 1: if the author exist
        Optional<FUser> userOptional = fUserMapper.findById(authorId);
        if (userOptional.isPresent()) {
            // 1. Generate post Id
            String generatedPostId = UUID.randomUUID().toString();
            // 2. Create a post
            Post post = new Post();
            post.postId = generatedPostId;
            post.author = userOptional.get();
            post.postContent = postContent;
            post.title = title;
            // 2. Save post
            postMapper.save(post);
            // 3. Save tags
            List<PostTag> postTags = new ArrayList<>();
            for (String tag : tags) {
                PostTag postTag = new PostTag();
                postTag.post = post;
                postTag.tagText = tag;
                postTags.add(postTag);
            }
            postTagMapper.saveAll(postTags);
        } else {
            throw new IllegalArgumentException("Author does not exist");
        }
    }

    public void deleteReply(String replyId, Long authorId) {
        // Check 1: if the reply exist
        Optional<Post> postOptional = postMapper.findById(replyId);
        if (postOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. delete the reply
                postMapper.deleteById(replyId);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Reply does not exist");
        }
    }

    public List<Post> getPostsByAuthorId(Long authorId){
        return postMapper.findAllByAuthorId(authorId);
    }


    @Resource
    private PostReplyMapper postReplyMapper;
    public void replyPost(String postId, String replyContent, Long authorId) {
        // Check 1: if the post exist
        Optional<Post> postOptional = postMapper.findById(postId);
        if (postOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. Create a reply
                PostReply postReply=new PostReply();
                postReply.replyContent = replyContent;
                postReply.author = userOptional.get();
                postReply.post = postOptional.get();
                // 2. Save reply
                postReplyMapper.save(postReply);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Post does not exist");
        }
    }

    public void deletePost(String postId, Long authorId) {
        // Check 1: if the post exist
        Optional<Post> postOptional = postMapper.findById(postId);
        if (postOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. delete the post
                postMapper.deleteById(postId);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Post does not exist");
        }
    }

}
