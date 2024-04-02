package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.FUser;
import cloud.igibgo.igibgobackend.entity.ResponseCodes;
import cloud.igibgo.igibgobackend.mapper.FUserMapper;
import cloud.igibgo.igibgobackend.util.MailUtil;
import cloud.igibgo.igibgobackend.util.PasswordUtil;
import cloud.igibgo.igibgobackend.util.StringUtil;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FUserService {
    @Resource
    FUserMapper fUserMapper;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    public APIResponse<String> userRegister1(String email, String password) {
        //Check 1: not Huili email
        if (!email.contains("@huilieducation.cn")) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Email must be @huilieducation.cn", null);
        }

        //Check 2: check if user is already registered
        if (fUserMapper.findByEmail(email) != null) {
            return new APIResponse<>(ResponseCodes.CONFLICT, "User already registered", null);
        }

        try {
            //1. Send email auth code
            String authCode = PasswordUtil.generateAuthCode();
            MailUtil.sendMail(email, "IGIBGO authentication code",
                    "Your authentication code is: " + authCode +
                            ". Please copy and paste the authentication code to the registration page in 5 minutes before it expire.");

            //2. Save to redis
            redisTemplate.opsForValue().set(email, password + authCode, 5, TimeUnit.MINUTES);
            return new APIResponse<>(ResponseCodes.SUCCESS, "Email sent successfully", null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    public APIResponse<String> userRegister2(String authCode,
                                             String email,
                                             String password,
                                             MultipartFile avatar) {
        // Check 1: check the auth code
        String redisValue = redisTemplate.opsForValue().get(email);// redis value contains password + auth code
        if (redisValue == null) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Auth code expired", null);
        }
        if (!redisValue.equals(password + authCode)) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Auth code incorrect", null);
        }
        //1. Delete the auth code
        redisTemplate.delete(email);
        //2. Upload avatar
        String avatarFileName = UUID.randomUUID().toString();
        try {
            Path tempAvatarFile = Files.createTempFile(null, avatarFileName);
            avatar.transferTo(tempAvatarFile);
            // time-consuming
            String avatarUrl = UploadUtil.upload(tempAvatarFile.toFile(), avatarFileName, "avatar/");
            boolean deletion = tempAvatarFile.toFile().delete();
            if (!deletion) {
                log.error("Failed to delete temp file: " + tempAvatarFile);
            }
            //3. Save to db
            if (StringUtil.isContainNumber(password)) {// not teacher
                fUserMapper.save(new FUser(false, email, password));
            } else {// is teacher
                fUserMapper.save(new FUser(true, email, password));
            }
            return new APIResponse<>(ResponseCodes.SUCCESS, "User registered successfully", avatarUrl);
        } catch (IOException e) {
            log.error("Failed to create temp file: ", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "File size exceeds 5G", null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }
}
