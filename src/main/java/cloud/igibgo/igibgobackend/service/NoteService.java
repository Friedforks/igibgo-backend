package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.ConstantUtil;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class NoteService {
    @Resource
    private NoteMapper noteMapper;

    @Transactional
    public Page<Note> getNotesInOrder(PageRequest pageRequest) {
        return noteMapper.findAll(pageRequest);
    }

    /**
     * O(NlogM) where N is the number of notes and M is the number of tags
     *
     * @param tags list of tags
     * @return set of notes that have at least one of the tags
     */
    public Page<Note> getNotesByTags(List<String> tags, PageRequest pageRequest) {
        Page<Note> notes = noteMapper.findAllByTag(tags, pageRequest);
        // remove duplicates
        Set<Note> noteSet = new HashSet<>(notes.getContent());
        return new PageImpl<>(new ArrayList<>(noteSet), notes.getPageable(), notes.getTotalElements());
    }

    public List<NoteReply> getAllReplies(String noteId) {
        return noteReplyMapper.findAllByNoteNoteIdOrderByReplyDateDesc(noteId);
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private NoteTagMapper noteTagMapper;

    @Resource
    private UploadUtil uploadUtil;

    public void uploadNote(MultipartFile note, Long authorId, Long collectionId, String title, List<String> tags)
            throws IOException {
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
            String originalFilename = note.getOriginalFilename();
            assert originalFilename != null;
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!suffix.equalsIgnoreCase("pdf")) {// only support pdf
                throw new IllegalArgumentException(
                        "File type not supported, please upload notes in PDF, MD or DOCX format");
            }
            // 1. Generate new file name
            String generatedNoteId = UUID.randomUUID().toString();
            String newFilename = generatedNoteId + "." + suffix;
            // 2. create the note folder if empty
            if (!Files.exists(Paths.get(ConstantUtil.tmpPath))) {
                Files.createDirectory(Paths.get(ConstantUtil.tmpPath));
            }
            Path tmpDir = Paths.get(ConstantUtil.tmpPath);
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }
            Path tmpNoteFile = Files.createTempFile(tmpDir, null, newFilename);
            note.transferTo(tmpNoteFile);
            // 3. upload note to COS
            String url = uploadUtil.upload(tmpNoteFile.toFile(), newFilename, "note/");
            // 4. fetch collection (if collection id is not null)
            Collection c = collection.get();
            Note noteInstance = new Note();
            noteInstance.noteId = generatedNoteId;
            noteInstance.author = author.get();
            noteInstance.collection = c;
            noteInstance.title = title;
            noteInstance.noteUrl = url;
            // 4. save note to db
            noteMapper.save(noteInstance);
            // 5. Save tags
            List<NoteTag> noteTags = new ArrayList<>();
            for (String tag : tags) {
                NoteTag noteTag = new NoteTag();
                noteTag.note = noteInstance;
                noteTag.tagText = tag;
                noteTagMapper.save(noteTag);
            }
            noteTagMapper.saveAll(noteTags);
        } else {
            throw new IllegalArgumentException("Author not found");
        }
    }

    @Resource
    private NoteLikeMapper noteLikeMapper;

    @Resource
    private NoteBookmarkMapper noteBookmarkMapper;

    void updateLikeCount(String noteId) {
        Long likeCount = noteLikeMapper.countByNoteNoteId(noteId);
        noteMapper.updateLikeCountByNoteId(noteId, likeCount);
    }

    void updateViewCount(String noteId) {
        Long viewCount = noteViewMapper.countByNoteNoteId(noteId);
        noteMapper.updateViewCountByNoteId(noteId, viewCount);
    }

    void updateSaveCount(String noteId) {
        Long saveCount = noteBookmarkMapper.countByNoteNoteId(noteId);
        noteMapper.updateSaveCountByNoteId(noteId, saveCount);
    }

    public void likeNote(String noteId, Long userId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        // Check1: if note exists
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found with the given note id");
        }
        // Check2: if user exists
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with the given user id");
        }
        // Check3: if the user has already liked the note
        Optional<NoteLike> noteLikeOptional = noteLikeMapper.findByNoteIdAndUserId(noteId, userId);
        if (noteLikeOptional.isPresent()) {
            throw new IllegalArgumentException("You have already liked the note");
        }
        Note note = noteOptional.get();
        FUser user = userOptional.get();
        NoteLike noteLike = new NoteLike();
        noteLike.note = note;
        noteLike.user = user;
        // save noteLike
        noteLikeMapper.save(noteLike);
        // update like count
        updateLikeCount(noteId);
    }

    public void unlikeNote(String noteId, Long userId) {
        Optional<NoteLike> noteLikeOptional = noteLikeMapper.findByNoteIdAndUserId(noteId, userId);
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        // Check 1: if the note exists
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found with the given note id");
        }
        // Check 2: if the user has liked the note
        if (noteLikeOptional.isEmpty()) {
            throw new IllegalArgumentException("You have not liked the note");
        }
        // Check 3: if the user exists
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with the given user id");
        }
        noteLikeMapper.deleteById(noteLikeOptional.get().noteLikeId);
        updateLikeCount(noteId);
    }

    @Resource
    private NoteViewMapper noteViewMapper;

    public Note getNoteByNoteId(String noteId, Long userId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        // Check 1: if the note exists
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found with the given note id");
        }
        // 1. get the note
        Note note = noteOptional.get();
        // 2. get the user
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        // Check 2: if the user exists
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found with the given user id");
        }
        FUser user = userOptional.get();
        // Check 3: if the user has already viewed the note
        Optional<NoteView> noteViewOptional = noteViewMapper.findByNoteIdAndUserId(noteId, userId);
        if (noteViewOptional.isPresent()) {
            return note;
        }
        // 3. save the user-note view record to db
        NoteView noteView = new NoteView();
        noteView.note = note;
        noteView.user = user;
        noteViewMapper.save(noteView);
        // 4. update the view count for note table
        updateViewCount(noteId);
        return note;
    }

    public Page<Note> getNotesByTitle(String title, PageRequest pageRequest) {
        return noteMapper.findAllByTitle(title, pageRequest);
    }

    public Set<String> getAllTags() {
        // fetch all distinct tag content from db
        List<String> tags = noteMapper.findAllTags();
        // convert to set to remove duplicate:
        Set<String> distinctTags = new HashSet<>(tags);
        return distinctTags;
    }

    public void deleteNote(Long author, String noteId) {
        // Check 1: if author exist
        Optional<FUser> authorOptional = fUserMapper.findById(author);
        if (authorOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        // Check 2: if note exists
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        // Check 3: if the author is the author of the note
        Note note = noteOptional.get();
        if (!note.author.userId.equals(author)) {
            throw new IllegalArgumentException("You are not the author of the note");
        }

        // 1. delete the note from COS
        // 1.1 convert public access url to file path in COS
        // E.g. public accessurl=
        // https://igibgo-1305786880.cos.ap-guangzhou.myqcloud.com/note/1b3e7b7b.pdf
        // and file path in COS= note/1b3e7b7b.pdf
        String filePath = "note/" + note.noteUrl.substring(note.noteUrl.indexOf("/"));
        uploadUtil.deleteObject(filePath);
        // 2. delete the note from db
        noteMapper.deleteById(noteId);
    }

    @Resource
    private NoteReplyMapper noteReplyMapper;

    public void replyNote(String noteId, String replyContent, Long authorId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        Optional<FUser> authorOptional = fUserMapper.findById(authorId);
        if (authorOptional.isEmpty()) {
            throw new IllegalArgumentException("Author not found");
        }
        Note note = noteOptional.get();
        NoteReply reply = new NoteReply();
        reply.replyContent = replyContent;
        reply.author = authorOptional.get();
        reply.note = note;
        noteReplyMapper.save(reply);
    }

    @Resource
    RedisTemplate<String, String> redisTemplate;

    public void deleteReply(Long replyId, String token) {
        // Check 1: verify the token
        String userEmail = redisTemplate.opsForValue().get(token);
        if (userEmail == null) {
            throw new IllegalArgumentException("User not logged in");
        }
        Optional<FUser> fUserOptional = fUserMapper.findByEmail(userEmail);
        if (fUserOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        // Check 2: if the reply exist
        if (noteReplyMapper.findById(replyId).isEmpty()) {
            throw new IllegalArgumentException("Reply not found");
        }
        noteReplyMapper.deleteById(replyId);
    }

    public Long noteTotalReply(String noteId) {
        return noteReplyMapper.countByNoteNoteId(noteId);
    }

    public Boolean isLiked(String noteId, Long userId) {
        return noteLikeMapper.findByNoteIdAndUserId(noteId, userId).isPresent();
    }

    public Boolean isSaved(String noteId, Long userId) {
        return !noteBookmarkMapper.findAllByNoteNoteIdAndBookmarkUserUserId(noteId, userId).isEmpty();
    }

    public Boolean isReplied(String noteId, Long userId) {
        return !noteReplyMapper.findNoteRepliesByNoteNoteIdAndAuthorUserId(noteId, userId).isEmpty();
    }

    @Resource
    private BookmarkMapper bookmarkMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public void bookmarkNote(String noteId, Long userId, List<String> bookmarkNames) {
        // Check 1: if the user exists
        Optional<FUser> userOptional = fUserMapper.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        // Check 2: if the note exists
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        Note note = noteOptional.get();
        FUser user = userOptional.get();
        Set<String> bookmarkNamesSet = new HashSet<>(bookmarkNames);// remove duplicate bookmark names
        List<Bookmark> bookmarks = new ArrayList<>();// parallel array with bookmarkNamesSet
        // 1. delete all note bookmarks for the note and user
        noteBookmarkMapper.deleteAllByBookmarkUserUserIdAndNoteNoteId(userId, noteId);
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
        // 4. Add note bookmarks
        for (Bookmark bookmark : bookmarks) {
            NoteBookmark noteBookmark = new NoteBookmark();
            noteBookmark.note = note;
            noteBookmark.bookmark = bookmark;
            noteBookmarkMapper.save(noteBookmark);// save to db
        }
        // 5. update the save count for the note
        updateSaveCount(noteId);
    }

    public List<NoteBookmark> getNoteBookmarksByUserIdAndNoteId(Long userId, String noteId) {
        return noteBookmarkMapper.findAllByNoteNoteIdAndBookmarkUserUserId(noteId, userId);
    }
}
