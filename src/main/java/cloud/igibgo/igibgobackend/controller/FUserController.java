package cloud.igibgo.igibgobackend.controller;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.service.FUserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fuser")
public class FUserController {
    @Resource
    FUserService fUserService;

    @PostMapping("/register1")
    public APIResponse<String> userRegister1(String email, String password) {
        return fUserService.userRegister1(email, password);
    }

    @PostMapping("/register2")
    public APIResponse<String> userRegister2(String authCode, String email, String password, MultipartFile avatar) {
        return fUserService.userRegister2(authCode, email, password,avatar);
    }

    /**
     * User login
     * @param email email
     * @param password password
     * @return token
     */
    @PostMapping("/login")
    public APIResponse<String> userLogin(String email, String password) {
        return fUserService.userLogin(email, password);
    }
}
