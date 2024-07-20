package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.ConstantUtil;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.PreUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class VideoService {
    @Resource
    private VideoMapper videoMapper;

    public Page<Video> getVideosInOrder(PageRequest pageRequest) {
        return videoMapper.findAll(pageRequest);
    }

    /**
     * O(NlogM) where N is the number of notes and M is the number of tags
     *
     * @param tags list of tags
     * @return list of videos that have at least one of the tags
     */
    public List<Video> getVideosByTags(List<String> tags) {
        Set<Video> videos = new HashSet<>();
        for (String tag : tags) {
            videos.addAll(videoMapper.findAllByTag(tag));
        }
        return videos.stream().toList();
    }


    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private VideoTagMapper videoTagMapper;
    @Resource
    private UploadUtil uploadUtil;

    /**
     * TODO Optimize this method to make video/videoCover save to COS and db in parallel
     */
    public void uploadVideo(MultipartFile video,
                            MultipartFile videoCover,
                            Long authorId,
                            Long collectionId,
                            String title,
                            List<String> tags) throws IOException {
        Optional<FUser> author = fUserMapper.findById(authorId);
        // Check 1: if the author  exist
        if (author.isPresent()) {
            // Check 2: if collection exist (if collection id is not null)
            /* it exists only when
             * 1. collection id is null
             * 2. collection id is not null and collection exists
             */
            Optional<Collection> collection = collectionMapper.findById(collectionId);
            if (collection.isEmpty()) {
                throw new IllegalArgumentException("Collection not found");
            }
            // Check 3: file type
            String originalFilename = video.getOriginalFilename();
            assert originalFilename != null;
            String videoSuffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!videoSuffix.equalsIgnoreCase("mp4") && !videoSuffix.equalsIgnoreCase("flv") && !videoSuffix.equalsIgnoreCase("mov")) {
                throw new IllegalArgumentException("File type not supported, please upload video in mp4, mov or flv format");
            }
            // 1. convert video to file
            String generatedVideoId = UUID.randomUUID().toString();
            String newFilename = generatedVideoId + "." + videoSuffix;
            Path tmpDir= Paths.get(ConstantUtil.tmpPath);
            Path tmpVideoFile = Files.createTempFile(tmpDir,null, newFilename);
            video.transferTo(tmpVideoFile);
            // 2. upload video to COS
            String url = uploadUtil.upload(tmpVideoFile.toFile(), newFilename, "video/");
            // 3. convert video cover to file
            String newCoverFilename = generatedVideoId + "-cover.png";
            Path tmpCoverFile = Files.createTempFile(tmpDir,null, newCoverFilename);
            videoCover.transferTo(tmpCoverFile);
            // 4. upload video cover to COS
            String coverUrl = uploadUtil.upload(tmpCoverFile.toFile(), newCoverFilename, "video-cover/");
            // 5. fetch collection (if collection id is not null)
            Collection c = collection.get();
            Video videoInstance = new Video();
            videoInstance.videoId = generatedVideoId;
            videoInstance.author = author.get();
            videoInstance.collection = c;
            videoInstance.title = title;
            videoInstance.videoUrl = url;
            videoInstance.videoCoverUrl = coverUrl;
            // 6. save note to db
            videoMapper.save(videoInstance);
            //7. Save tags
            List<VideoTag> videoTags = new ArrayList<>();
            for (String tag : tags) {
                VideoTag videoTag = new VideoTag();
                videoTag.video = videoInstance;
                videoTag.tagText = tag;
                videoTags.add(videoTag);
            }
            videoTagMapper.saveAll(videoTags);
        } else {
            throw new IllegalArgumentException("Author not found");
        }
    }

    public void likeVideo(String videoId) {
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        // if note exists
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();
            video.likeCount++;
            videoMapper.save(video);
        } else {
            throw new IllegalArgumentException("Video not found with the given video id");
        }
    }

    @Resource
    private VideoViewMapepr videoViewMapepr;

    private void updateViewCount(String videoId){
        Long viewCount=videoViewMapepr.countByVideoId(videoId);
        videoMapper.updateViewCountByVideoId(videoId,viewCount);
    }

    public Video getVideoByVideoId(String videoId, Long userId) {
        // TODO: Update the mechanism for incrementing view count (add video view table)
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        // Check 1: if the video exists
        if (videoOptional.isEmpty()) {
            throw new IllegalArgumentException("Video not found with the given video id");
        }
        // 1. get the video
        Video video = videoOptional.get();
        // 2. get the user
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        // Check 2: if the user exists
        if(userOptional.isEmpty()){
            throw new IllegalArgumentException("User not found with the given user id");
        }
        FUser user = userOptional.get();
        // Check 3: if the user has already viewed the video
        Optional<VideoView> videoViewOptional=videoViewMapepr.findByVideoIdAndUserId(videoId,userId);
        if(videoViewOptional.isPresent()){
            return video;
        }
        // 3. save the new video view record
        VideoView videoView = new VideoView();
        videoView.video = video;
        videoView.user = user;
        videoViewMapepr.save(videoView);

        // 4. update the view count
        updateViewCount(videoId);
        return video;
    }

    public void bookmarkVideo(String videoId, Long userId) {
        // Check 1: if the user exists
        if (fUserMapper.existsById(userId)) {
            // Check 2: if the video exists
            Optional<Video> video = videoMapper.findById(videoId);
            if (video.isPresent()) {
                // 1. get the video
                Video videoInstance = video.get();
                videoInstance.saveCount++;
                // 2. save the video to db
                videoMapper.save(videoInstance);
            } else {
                throw new IllegalArgumentException("Video not found with the given video id");
            }
        } else {
            throw new IllegalArgumentException("User not found with the given user id");
        }
    }

    public void deleteVideo(Long authorId, String videoId) {
        // Check 1: if author exists
        Optional<FUser> authorOptional = fUserMapper.findById(authorId);
        if (authorOptional.isEmpty()) {
            throw new IllegalArgumentException("Author not found");
        }
        // Check 2: if video exists
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        if (videoOptional.isEmpty()) {
            throw new IllegalArgumentException("Video not found");
        }
        // Check 3: if the author is the author of the video
        Video video = videoOptional.get();
        if (!video.author.userId.equals(authorId)) {
            throw new IllegalArgumentException("You are not the author of the video");
        }

        // 1. delete the video from COS
        // 1.1 convert public access url to file path in COS
        String videoPath = "video/" + video.videoUrl.substring(video.videoUrl.lastIndexOf("/"));
        uploadUtil.deleteObject(videoPath);
        // 2. delete the video cover from COS
        // 2.1 convert public access url to file path in COS
        String videoCoverPath = "video-cover/" + video.videoCoverUrl.substring(video.videoCoverUrl.lastIndexOf("/"));
        uploadUtil.deleteObject(videoCoverPath);
        // 3. delete the video from db
        videoMapper.delete(video);
    }

    @Resource
    private VideoReplyMapper videoReplyMapper;

    public void replyVideo(String videoId, String replyContent, Long authorId) {
        // Check 1: if the video exist
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        if (videoOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. Create a reply
                VideoReply videoReply = new VideoReply();
                videoReply.replyContent = replyContent;
                videoReply.author = userOptional.get();
                videoReply.video = videoOptional.get();
                // 2. Save reply
                videoReplyMapper.save(videoReply);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Video does not exist");
        }
    }

    public void deleteReply(Long replyId, Long authorId) {
        // Check 1: if the reply exist
        Optional<VideoReply> videoReplyOptional = videoReplyMapper.findById(replyId);
        if (videoReplyOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. delete the reply
                videoReplyMapper.deleteById(replyId);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Reply does not exist");
        }
    }
}
