package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.*;
import com.squareup.okhttp.Call;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.joda.time.IllegalFieldValueException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class FUserService {
    @Resource
    FUserMapper fUserMapper;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    MailUtil mailUtil;

    public APIResponse<String> userRegister1(String email, String password) {
        // Check 1: not Huili email
        if (!email.contains("@huilieducation.cn")) {
            log.info("User email: " + email + " try to register but failed since it is not Huili email");
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Email must be @huilieducation.cn", null);
        }

        // Check 2: check if user is already registered
        if (fUserMapper.findByEmail(email).isPresent()) {
            log.info("User email: " + email + " try to register but failed since it is already registered");
            return new APIResponse<>(ResponseCodes.CONFLICT, "User already registered", null);
        }

        try {
            // 1. Send email auth code
            String authCode = PasswordUtil.generateAuthCode();
            mailUtil.sendMail(email, "IGIBGO authentication code",
                    "Your authentication code is: " + authCode +
                            ". Please copy and paste the authentication code to the registration page in 5 minutes before it expire.");

            // 2. Save to redis
            redisTemplate.opsForValue().set("register" + email, password + authCode, 5, TimeUnit.MINUTES);
            return new APIResponse<>(ResponseCodes.SUCCESS, "Email sent successfully", null);
        } catch (Exception e) {
            log.error("Failed to send email or save to redis: ", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    @Resource
    UploadUtil uploadUtil;

    /**
     * User register 2
     *
     * @param username username
     * @param authCode email auth code
     * @param email    Huili email
     * @param password password
     * @param avatar   avatar file
     * @return token
     */
    public APIResponse<FUser> userRegister2(String username,
            String authCode,
            String email,
            String password,
            MultipartFile avatar) {
        // Check 1: check the auth code
        String redisValue = redisTemplate.opsForValue().get("register" + email);// redis value contains password + auth
                                                                                // code
        if (redisValue == null) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Wrong email entered or the auth code has expired.",
                    null);
        }
        if (!redisValue.equals(password + authCode)) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Auth code incorrect or wrong information was entered.",
                    null);
        }
        // 1. Delete the auth code
        redisTemplate.delete("register" + email);
        // 2. Upload avatar
        String avatarFileName = UUID.randomUUID() + Objects.requireNonNull(avatar.getOriginalFilename())
                .substring(avatar.getOriginalFilename().lastIndexOf("."));
        try {
            Path tmpDir = Paths.get(ConstantUtil.tmpPath);
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }
            Path tempAvatarFile = Files.createTempFile(tmpDir, null, avatarFileName);
            avatar.transferTo(tempAvatarFile);
            // time-consuming
            String avatarUrl = uploadUtil.upload(tempAvatarFile.toFile(), avatarFileName, "avatar/");
            // 3. encrypt password
            password = PasswordUtil.hashPassword(password);
            // 4. Save to db
            boolean isTeacher;
            isTeacher = !email.chars().anyMatch(Character::isDigit);
            FUser fUser = new FUser(
                    username,
                    avatarUrl,
                    isTeacher,
                    email,
                    password);
            fUserMapper.save(fUser);
            // 5. generate token
            String token = UUID.randomUUID().toString();
            fUser.token = token;
            redisTemplate.opsForValue().set(email, token);
            redisTemplate.opsForValue().set(token, email);
            return new APIResponse<>(ResponseCodes.SUCCESS, "User registered successfully", fUser);
        } catch (IOException e) {
            log.error("Failed to create temp file: ", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        } catch (IllegalArgumentException e) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "File size exceeds 5G", null);
        } catch (Exception e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
    }

    public FUser findFUser(Long userId, String token) throws IllegalArgumentException {
        Optional<FUser> fUserOptional = fUserMapper.findById(userId);
        if (fUserOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        FUser fUser = fUserOptional.get();
        // check if token is valid and user is the same
        if (token == null) {
            // hide sensitive info
            fUser.password = null;
            fUser.email = null;
            fUser.token = null;
        } else {
            // if token user isn't the same as the user requested
            String email = redisTemplate.opsForValue().get(token);
            if (email == null) {
                throw new IllegalArgumentException("User not logged in");
            }
            if (!email.equals(fUser.email)) {// user mismatch, hide sensitive info
                fUser.password = null;
                fUser.email = null;
                fUser.token = null;
            }
        }
        return fUser;
    }

    public APIResponse<FUser> login(String email, String password) {
        Optional<FUser> fUserOptional = fUserMapper.findByEmail(email);
        // Check 1: user not found
        if (fUserOptional.isEmpty()) {
            return new APIResponse<>(ResponseCodes.NOT_FOUND, "User not found. Please register first.", null);
        }
        // Check 2: password incorrect
        FUser fuser = fUserOptional.get();
        if (!fuser.password.equals(PasswordUtil.hashPassword(password))) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "Password incorrect", null);
        }
        // 1. Find if token is already generated in redis
        String token = redisTemplate.opsForValue().get(email);
        if (token == null) {
            // 2. Generate token
            token = UUID.randomUUID().toString();
            // 3. Save to redis
            redisTemplate.opsForValue().set(email, token, 1, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(token, email, 1, TimeUnit.DAYS);
        } else {
            // 4. Update token expiration time
            redisTemplate.expire(email, 1, TimeUnit.DAYS);
            redisTemplate.expire(token, 1, TimeUnit.DAYS);
        }
        fuser.token = token;
        // return token
        return new APIResponse<>(ResponseCodes.SUCCESS, "Login successfully ", fuser);
    }

    public void logout(String token) {
        // check 1: if token is null
        if (token == null) {
            log.error("Token is null");
            throw new IllegalArgumentException("Token is null");
        }
        // check 2: if token exists
        String email = redisTemplate.opsForValue().get(token);
        if (email != null) {
            redisTemplate.delete(email);
            redisTemplate.delete(token);
        } else {
            log.error("Token not found");
            throw new IllegalArgumentException("User already logged out");
        }
    }

    public FUser checkLogin(String token) {
        if (redisTemplate.opsForValue().get(token) == null) {
            throw new IllegalArgumentException("Token not found");
        }
        Optional<FUser> fUserOptional = fUserMapper.findByEmail(redisTemplate.opsForValue().get(token));
        if (fUserOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return fUserOptional.get();
    }

    public APIResponse<FUser> updateFUser(FUser fUser) {
        try {
            fUserMapper.save(fUser);
            return new APIResponse<>(ResponseCodes.SUCCESS, null, fUser);
        } catch (Exception e) {
            log.error("Failed to update user: ", e);
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }
    }

    @Resource
    private NoteLikeMapper noteLikeMapper;

    @Resource
    private VideoLikeMapper videoLikeMapper;

    public APIResponse<Long> totalLikes(Long authorId) throws ExecutionException, InterruptedException {
        // check 1: if user exists
        if (fUserMapper.findById(authorId).isEmpty()) {
            return new APIResponse<>(ResponseCodes.NOT_FOUND, "User not found", null);
        }
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        // 1.1 total note like
        Callable<Long> noteLikeCount = () -> noteLikeMapper.countByAuthorId(authorId);
        // 1.2 total video like
        Callable<Long> videoLikeCount = () -> videoLikeMapper.countByAuthorId(authorId);
        // 2. add total like
        Long totalLike = executor.submit(noteLikeCount).get() + executor.submit(videoLikeCount).get();
        return new APIResponse<>(ResponseCodes.SUCCESS, null, totalLike);
    }

    @Resource
    private NoteBookmarkMapper noteBookmarkMapper;

    @Resource
    private VideoBookmarkMapper videoBookmarkMapper;

    // TODO: add saves from video
    public APIResponse<Long> totalSaves(Long userId) throws ExecutionException, InterruptedException {
        // check 1: if user exists
        if (fUserMapper.findById(userId).isEmpty()) {
            return new APIResponse<>(ResponseCodes.NOT_FOUND, "User not found", null);
        }
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        // 1.1 total note save
        Callable<Long> noteSaveCount = () -> noteBookmarkMapper.countByAuthorId(userId);
        // 1.2 total video save
        Callable<Long> videoSaveCount = () -> videoBookmarkMapper.countByAuthorId(userId);
        // 2. add total save
        Long saveCount = executor.submit(noteSaveCount).get() + executor.submit(videoSaveCount).get();
        return new APIResponse<>(ResponseCodes.SUCCESS, null, saveCount);
    }

    @Resource
    private NoteViewMapper noteViewMapper;

    @Resource
    private VideoViewMapepr videoViewMapepr;

    public APIResponse<Long> totalViews(Long userId) throws ExecutionException, InterruptedException {
        // check 1: if user exists
        if (fUserMapper.findById(userId).isEmpty()) {
            return new APIResponse<>(ResponseCodes.NOT_FOUND, "User not found", null);
        }
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        // 1.1 total note view
        Callable<Long> noteViewCount = () -> noteViewMapper.countByAuthorId(userId);
        // 1.2 total video view
        Callable<Long> videoViewCount = () -> videoViewMapepr.countByAuthorId(userId);
        Long totalView = executor.submit(noteViewCount).get() + executor.submit(videoViewCount).get();
        return new APIResponse<>(ResponseCodes.SUCCESS, null, totalView);
    }

    public APIResponse<FUser> updateAvatar(String token, MultipartFile avatar) {
        // check 1: if token exists
        String email = redisTemplate.opsForValue().get(token);
        if (email == null) {
            return new APIResponse<>(ResponseCodes.BAD_REQUEST, "User not logged in", null);
        }
        // check 2: if user exists
        Optional<FUser> fUserOptional = fUserMapper.findByEmail(email);
        if (fUserOptional.isEmpty()) {
            return new APIResponse<>(ResponseCodes.NOT_FOUND, "User not found", null);
        }
        FUser fUser = fUserOptional.get();
        // 1. upload the new avatar
        String avatarFileName = UUID.randomUUID() + Objects.requireNonNull(avatar.getOriginalFilename())
                .substring(avatar.getOriginalFilename().lastIndexOf("."));
        try {
            Path tmpDir = Paths.get(ConstantUtil.tmpPath);
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }
            Path tempAvatarFile = Files.createTempFile(tmpDir, null, avatarFileName);
            avatar.transferTo(tempAvatarFile);
            // parallel processing (2,3) and (4) using virtual threads
            // time-consuming
            // for example
            // avatarUrl="https://igibgo-1306825637.cos.ap-shanghai.myqcloud.com/avatar/1b9d6bc7-4f3f-4f3f-8f3f-4f3f8f3f4f3f.jpg"
            String avatarUrl = uploadUtil.upload(tempAvatarFile.toFile(), avatarFileName, "avatar/");
            // 2. update the avatar
            fUser.avatarUrl = avatarUrl;
            // 3. save to db
            fUserMapper.save(fUser);
            // 4. delete the old avatar
            String oldAvatarFileName = fUser.avatarUrl.substring(fUser.avatarUrl.lastIndexOf("/") + 1);
            uploadUtil.deleteObject("avatar/" + oldAvatarFileName);
        } catch (IOException e) {
            return new APIResponse<>(ResponseCodes.INTERNAL_SERVER_ERROR, "Internal server error", null);
        }
        return new APIResponse<>(ResponseCodes.SUCCESS, null, fUser);
    }

    @Resource
    private BookmarkMapper bookmarkMapper;

    public List<Bookmark> getBookmarksByUserId(Long userId) {
        return bookmarkMapper.findAllByUserUserId(userId);
    }

    public Optional<FUser> findFuserByUserId(Long userId) {
        return fUserMapper.findById(userId);
    }
}
