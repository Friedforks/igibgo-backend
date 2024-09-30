package cloud.igibgo.igibgobackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cloud.igibgo.igibgobackend.entity.response.APIResponse;
import cloud.igibgo.igibgobackend.entity.FUser.Bookmark;
import cloud.igibgo.igibgobackend.entity.response.ResponseCodes;
import cloud.igibgo.igibgobackend.service.BookmarkService;
import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequestMapping("/bookmark")
public class BookmarkController {
    @Resource
    private BookmarkService bookmarkService;


    @GetMapping("/get/by/userId")
    public APIResponse<List<Bookmark>> getBookmarkByUserId(Long userId){
        try{
            List<Bookmark> bookmarks = bookmarkService.getBookmarkByUserId(userId);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, bookmarks);
        }catch (Exception e){
            return new APIResponse<List<Bookmark>>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }
}
