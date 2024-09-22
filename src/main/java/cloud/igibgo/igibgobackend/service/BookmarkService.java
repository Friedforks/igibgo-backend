package cloud.igibgo.igibgobackend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cloud.igibgo.igibgobackend.entity.Bookmark;
import cloud.igibgo.igibgobackend.entity.FUser;
import cloud.igibgo.igibgobackend.mapper.BookmarkMapper;
import cloud.igibgo.igibgobackend.mapper.NoteBookmarkMapper;
import cloud.igibgo.igibgobackend.mapper.VideoBookmarkMapper;
import jakarta.annotation.Resource;

@Service
public class BookmarkService {
    @Resource
    private BookmarkMapper bookmarkMapper;

    @Resource
    private NoteBookmarkMapper noteBookmarkMapper;

    @Resource
    private VideoBookmarkMapper videoBookmarkMapper;

    @Resource
    private FUserService fUserService;

    public List<Bookmark> getBookmarkByUserId(Long userId) {
        // Check 1: if user exist
        fUserService.findFUser(userId,null);
        List<Bookmark> bookmarks = bookmarkMapper.findAllByUserUserId(userId);
        return bookmarks;
    }
}