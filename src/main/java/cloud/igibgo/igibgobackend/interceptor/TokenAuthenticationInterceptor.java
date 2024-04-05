package cloud.igibgo.igibgobackend.interceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenAuthenticationInterceptor implements HandlerInterceptor {
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equalsIgnoreCase("OPTIONS")){
            log.info("Returned OPTIONS");
            return true;
        }
        // log the path of the request
        String path=request.getRequestURL().toString();
        log.debug("Login interface intercepted, path: {}",path);

        // get the token from the request header
        String token=request.getHeader("token");
        log.debug("Starting login verification: token: {}",token);

        // if the token is empty, return false
        if(token==null||token.isEmpty()){
            log.info("Token is empty, request denied");
            return false;
        }

        // if the token is invalid, return false
        String s=redisTemplate.opsForValue().get(token);
        if(s==null||s.isEmpty()){
            log.warn("Token is invalid, request denied");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());// 401
            return false;
        }
        else{
            log.info("Successfully logged in: {}",s);
            return true;
        }
    }
}

