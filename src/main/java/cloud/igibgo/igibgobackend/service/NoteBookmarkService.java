package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.Bookmark;
import cloud.igibgo.igibgobackend.entity.FUser;
import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.entity.NoteBookmark;
import cloud.igibgo.igibgobackend.mapper.BookmarkMapper;
import cloud.igibgo.igibgobackend.mapper.FUserMapper;
import cloud.igibgo.igibgobackend.mapper.NoteBookmarkMapper;
import cloud.igibgo.igibgobackend.mapper.NoteMapper;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;

@Service
public class NoteBookmarkService {
    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private NoteBookmarkMapper noteBookmarkMapper;

    @Resource
    private NoteService noteService;
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
        entityManager.clear();// IMPORTANT: clear the entity manager to avoid stale data, TOOK ME SO LONG TO FIX THIS PROBLEM!
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
            // there's no .isPresent check it must exist after the above check
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
    }

    public List<Bookmark> getBookmarksByUserId(Long userId) {
        return bookmarkMapper.findAllByUserUserId(userId);
    }

    public List<NoteBookmark> getBookmarksByUserIdAndNoteId(Long userId, String noteId) {
        return noteBookmarkMapper.findAllByNoteNoteIdAndBookmarkUserUserId(noteId, userId);
    }
}
