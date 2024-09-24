package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.ConstantUtil;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

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

    public Page<Video> getVideosByVideoTitle(String videoTitle, Pageable pageable) {
        return videoMapper.searchByTitle(videoTitle, pageable);
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private VideoTagMapper videoTagMapper;
    @Resource
    private UploadUtil uploadUtil;

    public void uploadVideo(MultipartFile video,
            MultipartFile videoCover,
            Long authorId,
            Long collectionId,
            String title,
            List<String> tags) throws IOException, ExecutionException, InterruptedException {
        Optional<FUser> author = fUserMapper.findById(authorId);
        // Check 1: if the author exist
        if (author.isPresent()) {
            // Check 2: if collection exist (if collection id is not null)
            /*
             * it exists only when
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
            // match video suffix
            List<String> supportedSuffixList = List.of("mp4", "flv", "mov", "mkv");
            if (supportedSuffixList.stream().noneMatch(suffix -> suffix.equalsIgnoreCase(videoSuffix))) {
                throw new IllegalArgumentException(
                        "File type not supported, please upload video in mp4, flv, mov and mkv format");
            }

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

            String generatedVideoId = UUID.randomUUID().toString();
            Callable<String> uploadVideoTask = () -> {
                // 1.1 convert video to file
                String newFilename = generatedVideoId + "." + videoSuffix;
                Path tmpDir = Paths.get(ConstantUtil.tmpPath);
                if (!Files.exists(tmpDir)) {
                    Files.createDirectories(tmpDir);
                }
                Path tmpVideoFile = Files.createTempFile(tmpDir, null, newFilename);
                video.transferTo(tmpVideoFile);
                // 1.2 upload video to COS
                return uploadUtil.upload(tmpVideoFile.toFile(), newFilename, "video/");
            };
            Callable<String> uploadCoverTask = () -> {
                // 2.1 convert video cover to file
                String newCoverFilename = generatedVideoId + "-cover.png";
                Path tmpDir = Paths.get(ConstantUtil.tmpPath);
                if (!Files.exists(tmpDir)) {
                    Files.createDirectories(tmpDir);
                }
                Path tmpCoverFile = Files.createTempFile(tmpDir, null, newCoverFilename);
                videoCover.transferTo(tmpCoverFile);
                // 2.2 upload video cover to COS
                return uploadUtil.upload(tmpCoverFile.toFile(), newCoverFilename, "video-cover/");
            };

            // 3. submit tasks for execution
            String videoUrl = executor.submit(uploadVideoTask).get();
            String coverUrl = executor.submit(uploadCoverTask).get();

            // 4. fetch collection (if collection id is not null)
            Collection c = collection.get();
            Video videoRecord = new Video();
            videoRecord.videoId = generatedVideoId;
            videoRecord.author = author.get();
            videoRecord.collection = c;
            videoRecord.title = title;
            videoRecord.videoUrl = videoUrl;
            videoRecord.videoCoverUrl = coverUrl;

            // 5. save video to db
            videoMapper.save(videoRecord);

            // 6. Save tags
            List<VideoTag> videoTags = new ArrayList<>();
            for (String tag : tags) {
                VideoTag videoTag = new VideoTag();
                videoTag.video = videoRecord;
                videoTag.tagText = tag;
                videoTags.add(videoTag);
            }
            videoTagMapper.saveAll(videoTags);
            executor.shutdown();
        } else {
            throw new IllegalArgumentException("Author not found");
        }
    }

    @Resource
    private VideoLikeMapper videoLikeMapper;

    public void likeVideo(String videoId, Long userId) {
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        // Check 1: if video exist
        if (videoOptional.isEmpty())
            throw new IllegalArgumentException("Video not found with the given video id");
        // Check 2: if user exist
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty())
            throw new IllegalArgumentException("User not found with the given user id");
        // Check 3: if user has already liked the video
        Optional<VideoLike> videoLikeOptional = videoLikeMapper.findByVideoIdAndUserId(videoId, userId);
        if (videoLikeOptional.isPresent())
            return;// no error since the user might want to unlike the video (click too often)

        Video video = videoOptional.get();
        FUser user = userOptional.get();

        // 1. save new like record to video_like table
        VideoLike videoLike = new VideoLike();
        videoLike.video = video;
        videoLike.user = user;
        videoLikeMapper.save(videoLike);

        // 2. update the like count in video table
        updateLikeCount(videoId);
    }

    public void unlikeVideo(String videoId, Long userId) {
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        // Check 1: if video exist
        if (videoOptional.isEmpty())
            throw new IllegalArgumentException("Video not found with the given video id");
        // Check 2: if user exist
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty())
            throw new IllegalArgumentException("User not found with the given user id");
        // Check 3: if user has already liked the video
        Optional<VideoLike> videoLikeOptional = videoLikeMapper.findByVideoIdAndUserId(videoId, userId);
        if (videoLikeOptional.isEmpty())
            return;// no error since the user might want to like the video (click too often)

        // 1. delete the like record from video_like table
        videoLikeMapper.delete(videoLikeOptional.get());

        // 2. update the like count in video table
        updateLikeCount(videoId);
    }

    public boolean isLiked(String videoId, Long userId) {
        Optional<VideoLike> videoLikeOptional = videoLikeMapper.findByVideoIdAndUserId(videoId, userId);
        return videoLikeOptional.isPresent();
    }

    public boolean isSaved(String videoId, Long userId) {
        List<VideoBookmark> videoBookmarkOptional = videoBookmarkMapper
                .findAllByVideoVideoIdAndBookmarkUserUserId(videoId, userId);
        return !videoBookmarkOptional.isEmpty();// not empty
    }

    @Resource
    private VideoViewMapepr videoViewMapepr;

    private void updateViewCount(String videoId) {
        Long viewCount = videoViewMapepr.countByVideoId(videoId);
        videoMapper.updateViewCountByVideoId(videoId, viewCount);
    }

    private void updateLikeCount(String videoId) {
        Long likeCount = videoLikeMapper.countByVideoVideoId(videoId);
        videoMapper.updateLikeCountByVideoId(videoId, likeCount);
    }

    private void updateSaveCount(String videoId) {
        Long saveCount = videoBookmarkMapper.countByVideoVideoId(videoId);
        videoMapper.updateSaveCountByVideoId(videoId, saveCount);
    }

    public Video getVideoByVideoId(String videoId, Long userId) {
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
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with the given user id");
        }
        FUser user = userOptional.get();
        // Check 3: if the user has already viewed the video
        Optional<VideoView> videoViewOptional = videoViewMapepr.findByVideoIdAndUserId(videoId, userId);
        if (videoViewOptional.isPresent()) {
            return video;
        }
        // 3. save the new video view record
        VideoView videoView = new VideoView();
        videoView.video = video;
        videoView.user = user;
        videoViewMapepr.save(videoView);

        // 4. update the view count
        updateViewCount(videoId);

        // 5. update author view count

        return video;
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

    @Resource
    private VideoBookmarkMapper videoBookmarkMapper;

    public List<VideoBookmark> getVideoBookmarksByUserIdAndVideoId(Long userId, String videoId) {
        return videoBookmarkMapper.findAllByVideoVideoIdAndBookmarkUserUserId(videoId, userId);
    }

    @Resource
    private BookmarkMapper bookmarkMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public void bookmarkVideo(String videoId, Long userId, List<String> bookmarkNames) {
        // Check 1: if the user exists
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        // Check 2: if the video exists
        Optional<Video> videoOptional = videoMapper.findById(videoId);
        if (videoOptional.isEmpty()) {
            throw new IllegalArgumentException("Video not found");
        }
        Video video = videoOptional.get();
        FUser user = userOptional.get();
        Set<String> bookmarkNamesSet = new HashSet<>(bookmarkNames);// remove duplicate bookmark names
        List<Bookmark> bookmarks = new ArrayList<>();// parallel array with bookmarkNamesSet
        // 1. delete all note bookmarks for the note and user
        videoBookmarkMapper.deleteByVideoVideoIdAndBookmarkUserUserId(videoId, userId);
        // 2. delete all bookmarks with no note bookmarks
        entityManager.clear();// IMPORTANT: clear the entity manager to avoid stale data, TOOK ME SO LONG TO
                              // FIX THIS PROBLEM!
        List<Bookmark> allBookmarks = bookmarkMapper.findAllByUserUserId(userId);
        for (Bookmark bookmark : allBookmarks) {
            if (bookmark.noteBookmarks.isEmpty()) {
                bookmarkMapper.deleteById(bookmark.bookmarkId);
            }
        }
        // Add bookmarks
        for (String bookmarkName : bookmarkNamesSet) {
            // 3. check if the bookmarkName exists in the bookmark table
            Optional<Bookmark> bookmarkOptional = bookmarkMapper.findByBookmarkName(bookmarkName);
            if (bookmarkOptional.isEmpty()) {// the bookmark name is not in the table, create a new bookmark
                Bookmark bookmark = new Bookmark();
                bookmark.user = user;
                bookmark.bookmarkName = bookmarkName;
                bookmarkMapper.save(bookmark);// save to db
            }
            // 4. add the bookmark to the list (for later use)
            // there's no .isPresent check since the bookmark must exist after the above
            // check
            Bookmark bookmark = bookmarkMapper.findByBookmarkName(bookmarkName).get();
            bookmarks.add(bookmark);
        }
        // 4. Add video bookmarks
        for (Bookmark bookmark : bookmarks) {
            VideoBookmark videoBookmark = new VideoBookmark();
            videoBookmark.video = video;
            videoBookmark.bookmark = bookmark;
            videoBookmarkMapper.save(videoBookmark);// save to db
        }
        // 5. Update save count
        updateSaveCount(videoId);
    }

    @Resource
    private FUserService fUserService;

    public List<Video> getAllVideosByUserId(Long userId) {
        // check 1: user exist
        fUserService.findFUser(userId, null);
        return videoMapper.findAllByAuthorUserId(userId);
    }
}
