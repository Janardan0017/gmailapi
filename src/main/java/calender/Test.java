package calender;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by emp350 on 04/03/21
 */
public class Test {

    public static void main(String[] args) {
//        String[] availableIDs = TimeZone.getAvailableIDs();
//        for(String s: availableIDs) {
//            System.out.println(s);
//        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        System.out.println(dateFormat.format(System.currentTimeMillis()));
    }
}
