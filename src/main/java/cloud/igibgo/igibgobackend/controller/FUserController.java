package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.FUser;
import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.mapper.NoteMapper;
import cloud.igibgo.igibgobackend.service.FUserService;
import jakarta.annotation.Resource;
import org.aspectj.weaver.ast.Not;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * @param email email
     * @param password password
     * @return token
     */
    @PostMapping("/login")
    public APIResponse<FUser> login(String email, String password) {
        return fUserService.login(email, password);
    }

    @PostMapping("/logout")
    public APIResponse<Void> logout(String token) {
        try{
            fUserService.logout(token);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, null);
        }catch (IllegalArgumentException e){
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        }catch (Exception e){
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @PostMapping("/checkLogin")
    public APIResponse<FUser> checkLogin(String token) {
        try{
            FUser fUser = fUserService.checkLogin(token);
            return new APIResponse<>(ResponseCodes.SUCCESS,null,fUser);
        }catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, e.getMessage(), null);
        }
        catch (Exception e){
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @GetMapping("/userId")
    public APIResponse<FUser> findFUser(Long userId){
        return fUserService.findFUser(userId);
    }

    @PostMapping("/update")
    public APIResponse<FUser> updateFUser(FUser fUser){
        return fUserService.updateFUser(fUser);
    }

    // statistics
    @GetMapping("/total/like")
    public APIResponse<Long> totalLikes(Long userId){
        return fUserService.totalLikes(userId);
    }

    @GetMapping("/total/save")
    public APIResponse<Long> totalSaves(Long userId){
        return fUserService.totalSaves(userId);
    }

    @GetMapping("/total/view")
    public APIResponse<Long> totalViews(Long userId){
        return fUserService.totalViews(userId);
    }
}
