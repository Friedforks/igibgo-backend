package cloud.igibgo.igibgobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class IgibgoBackendApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+08:00"));
        SpringApplication.run(IgibgoBackendApplication.class, args);
    }
}
