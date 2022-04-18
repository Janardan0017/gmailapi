package calender;

import java.util.Date;
import java.util.List;

/**
 * Created by emp350 on 02/03/21
 */
public class EventQdo {

    public String summary;
    public String location;
    public String description;

    public Date startDate;
    public Date endDate;
    public String timeZone;

    public List<String> attendeeEmails;

    public boolean sendEmailReminder;
    //Number of minutes before the start of the event when the reminder should trigger. Valid values
    //are between 0 and 40320 (4 weeks in minutes)
    public int emailReminderBeforeMinutes;

    public boolean sendPopupReminder;
    //Number of minutes before the start of the event when the reminder should trigger. Valid values
    //are between 0 and 40320 (4 weeks in minutes)
    public int popupReminderBeforeMinutes;

    public RecurringQdo recurringQdo;


    public EventQdo(String summary, String location, String description, Date startDate, Date endDate, String timeZone,
                    List<String> attendeeEmails, boolean sendEmailReminder, int emailReminderBeforeMinutes, boolean sendPopupReminder,
                    int popupReminderBeforeMinutes, Frequency frequency, Date endTime, int interval) {
        this.summary = summary;
        this.location = location;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeZone = timeZone;
        this.attendeeEmails = attendeeEmails;
        this.sendEmailReminder = sendEmailReminder;
        this.emailReminderBeforeMinutes = emailReminderBeforeMinutes;
        this.sendPopupReminder = sendPopupReminder;
        this.popupReminderBeforeMinutes = popupReminderBeforeMinutes;
        this.recurringQdo = new RecurringQdo(frequency, endTime, interval);
    }
}
