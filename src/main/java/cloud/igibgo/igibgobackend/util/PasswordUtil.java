package cloud.igibgo.igibgobackend.util;

public class PasswordUtil {
    public static String generateAuthCode(){
        // randomly generate a string (length 6) containing number and characters
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder authCode = new StringBuilder();
        for(int i=0;i<6;i++){
            authCode.append(str.charAt((int)(Math.random()*str.length())));
        }
        return authCode.toString();
    }
}
