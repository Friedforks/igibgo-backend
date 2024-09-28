package cloud.igibgo.igibgobackend.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {
    @Value("${util.tmpDir}")
    private String tmpDir;

    @PostConstruct
    public void init() {
        ConstantUtil.tmpPath = tmpDir;
    }
}
