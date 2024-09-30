package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.FUser.Bookmark;
import cloud.igibgo.igibgobackend.entity.FUser.FUser;
import cloud.igibgo.igibgobackend.entity.response.APIResponse;
import cloud.igibgo.igibgobackend.entity.response.ResponseCodes;
import cloud.igibgo.igibgobackend.service.FUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/fuser")
public class FUserController {
    @Resource
    FUserService fUserService;

    @PostMapping("/register1")
    public APIResponse<String> userRegister1(String email, String password) {
        return fUserService.userRegister1(email, password);
    }

    /**
     * User register stage 2
     *
     * @param username username
     * @param authCode auth code
     * @param email    email
     * @param password password
     * @param avatar   avatar url
     * @return user containing token
     */
    @PostMapping("/register2")
    public APIResponse<FUser> userRegister2(String username,
                                            String authCode,
                                            String email,
                                            String password,
                                            MultipartFile avatar) {
        return fUserService.userRegister2(username, authCode, email, password, avatar);
    }

    /**
     * User login
     *
     * @param email    email
     * @param password password
     * @return token
     */
    @PostMapping("/login")
    public APIResponse<FUser> login(String email, String password) {
        return fUserService.login(email, password);
    }

    @PostMapping("/logout")
    public APIResponse<Void> logout(String token) {
        try {
            fUserService.logout(token);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @PostMapping("/checkLogin")
    public APIResponse<FUser> checkLogin(String token) {
        try {
            FUser fUser = fUserService.checkLogin(token);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, fUser);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @GetMapping("/userId")
    public APIResponse<FUser> findFUser(Long userId, String token) {
        try {
            return new APIResponse<FUser>(ResponseCodes.SUCCESS, null, fUserService.findFUser(userId, token));
        } catch (IllegalArgumentException e) {
            return new APIResponse<FUser>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<FUser>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }


    // statistics
    @GetMapping("/total/like")
    public APIResponse<Long> totalLikes(Long userId) {
        try {
            return fUserService.totalLikes(userId);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @GetMapping("/total/save")
    public APIResponse<Long> totalSaves(Long userId) {
        try {
            return fUserService.totalSaves(userId);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @GetMapping("/total/view")
    public APIResponse<Long> totalViews(Long userId) {
        try {
            return fUserService.totalViews(userId);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @PostMapping("/update/password")
    public APIResponse<Void> updatePassword(String token, String currentPassword, String newPassword) {
        try {
            return fUserService.updatePassword(token, currentPassword, newPassword);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @PostMapping("/update/username")
    public APIResponse<Void> updateUsername(String token, String newUsername) {
        try {
            fUserService.updateUsername(token, newUsername);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @PostMapping("/update/avatar")
    public APIResponse<Void> updateAvatar(String token, MultipartFile avatar) {
        try {
            fUserService.updateAvatar(token, avatar);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @GetMapping("/bookmark/get/userId")
    APIResponse<List<Bookmark>> getBookmarksByUserId(Long userId) {
        try {
            return new APIResponse<>(ResponseCodes.SUCCESS, null, fUserService.getBookmarksByUserId(userId));
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        } catch (DataAccessException e) {
            log.error("Database query error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Database query error", null);
        } catch (Exception e) {
            log.error("Unhandled error: {}", e.getMessage(), e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
