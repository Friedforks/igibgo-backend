package cloud.igibgo.igibgobackend.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MailUtil {
    @Resource
    private JavaMailSender mailSender;

    /**
     * Send email
     *
     * @param to      receiver
     * @param subject subject
     * @param text    content
     */

     @Value("${spring.mail.username}")
     private String from;

    public void sendMail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);// sender
        message.setTo(to);// receiver
        message.setSubject(subject);// subject
        message.setText(text);// content
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to "+to+": ", e);
        }
    }
}
