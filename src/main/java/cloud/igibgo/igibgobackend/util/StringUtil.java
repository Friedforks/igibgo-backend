package cloud.igibgo.igibgobackend.util;

public class StringUtil {
    // check if a string contains numbers
    public static boolean isContainNumber(String str){
        for (int i = 0; i < str.length(); i++){
            if (Character.isDigit(str.charAt(i))){
                return true;
            }
        }
        return false;
    }
}
