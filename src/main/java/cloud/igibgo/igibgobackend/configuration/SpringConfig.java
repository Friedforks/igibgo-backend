package cloud.igibgo.igibgobackend.configuration;

import cloud.igibgo.igibgobackend.interceptor.TokenAuthenticationInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringConfig implements WebMvcConfigurer {
    @Resource
    TokenAuthenticationInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // add login interceptor (check login for backstage)
                .addPathPatterns("/**")
                .excludePathPatterns("/fuser/**");
    }
}
