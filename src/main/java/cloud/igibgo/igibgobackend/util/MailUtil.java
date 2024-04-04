package cloud.igibgo.igibgobackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
public class MailUtil {
    private static JavaMailSender mailSender;

    /**
     * Send email
     *
     * @param to      receiver
     * @param subject subject
     * @param text    content
     */
    public static void sendMail(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("3344544482@qq.com");// sender
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
