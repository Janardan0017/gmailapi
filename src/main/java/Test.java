import java.sql.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Copyright © FieldCircle. All rights Reserved. by Jovanta Consultants Private Limited.
 * <p>
 * Created by emp350 on 07/04/21
 */
public class Test {

    public static void main(String[] args) {

        String[] availableIDs = TimeZone.getAvailableIDs();
        for(String s: availableIDs) {
            TimeZone timeZone = TimeZone.getTimeZone(s);
//            System.out.println(timeZone.getDisplayName());
            if(timeZone.getDisplayName().equals("Niue Time")){
                System.out.println("Match found");
                System.out.println(timeZone.getID());
                System.out.println(timeZone.getDSTSavings());
                System.out.println(timeZone.getRawOffset());
            }
        }

    }
}
