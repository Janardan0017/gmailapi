package calender;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import gmail.GmailQuickstart;
import gmail.models.SignUpUserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class CalendarQuickstart {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String REFRESH_TOKEN = "1//0gskTolRrb-F7CgYIARAAGBASNwF-L9IrjewAmiO5lvQRv10LFoEeBfiqFsRe4CsjPU19ojDLyorpNGDy3gPA2wnqgLk4VBO0Dzc";
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final String CREDENTIALS_FILE_PATH = "/ankur_cred.json";

    public static void main(String... args) throws IOException {

        SignUpUserDetails userDetails = getAccessTokenFromCode("4/0AY0e-g7xGj9cMhIrxLOFc2HCXYr9DK7Tr0Ax_GdCbQwEv2J4PMIAXVD2sZRnd68i12TfiA");
        System.out.println(userDetails.refreshToken);

        String allDay = "SUN;MON;TUE;WED;THU;FRI;SAT";
        String strings = Arrays.stream(allDay.split(";")).map(day -> day.substring(0, 2)).collect(Collectors.joining(","));
        System.out.println(strings);
        long millis = System.currentTimeMillis();
        EventQdo eventQdo = new EventQdo("Date Matching", "Noida", "event", new Date(millis + 3600000),
                new Date(millis + (2 * 60 * 60 * 1000)), "Asia/Kolkata", null, false,
                60, false, 60, Frequency.WEEKLY, new Date(millis+(5*24*60*60*1000)), 1);
//        createEvent(eventQdo);


//        List<Event> events = getAllInstances("primary", "u5sh9uuqbis4oklnl64ff3080g");
//        events.forEach(instance -> {
//            System.out.println(instance.getId());
//            System.out.println(instance.getRecurringEventId());
//            System.out.println(instance.getSummary());
//            DateTime start = instance.getStart().getDateTime();
//            System.out.println(start.getValue());
//            Date date = new Date(start.getValue());
//            TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
//            DateTime dateTime = new DateTime(date, timeZone);
//            System.out.println(dateTime);
//            System.out.println(start.equals(dateTime));
//            System.out.println(start);
//            System.out.println(instance.getEnd().getDateTime());
//            System.out.println("#########################################################################");
//        });

//        updateAnInstance("primary", "impvra3sok4rqc2pv356ulqhks", "impvra3sok4rqc2pv356ulqhks_20210321T143900Z", eventQdo);

//        cancelAnInstance("primary", "tqmtbal9rnlbrs0npq55uamse8", "tqmtbal9rnlbrs0npq55uamse8_20210318T134748Z");

//        getEvents(service);

//        updateEvent("primary", "bsjdhsl16ak6drpano4bd3n5pc_20210323T112946Z", eventQdo);

//        deleteEvent("primary", "bcirplpj164qiild2mnagmh9qc");

    }

    public static void updateAnInstance(String calendarId, String eventId, String instanceId, EventQdo eventQdo) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        if (service == null) {
            return;
        }
        List<Event> items = service.events().instances(calendarId, eventId).execute().getItems();
        Event event = items.stream().filter(item -> item.getId().equals(instanceId)).findFirst().orElse(null);

        event.setSummary(eventQdo.summary)
                .setLocation(eventQdo.location)
                .setDescription(eventQdo.description);

        DateTime startDateTime = new DateTime(eventQdo.startDate);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setStart(start);

        DateTime endDateTime = new DateTime(eventQdo.endDate);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setEnd(end);

//        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
//        event.setRecurrence(Arrays.asList(recurrence));

        List<EventReminder> eventReminders = new ArrayList<>();
        if (eventQdo.sendEmailReminder) {
            eventReminders.add(new EventReminder().setMethod("email").setMinutes(eventQdo.emailReminderBeforeMinutes));
        }
        if (eventQdo.sendPopupReminder) {
            eventReminders.add(new EventReminder().setMethod("popup").setMinutes(eventQdo.popupReminderBeforeMinutes));
        }
        if (!eventReminders.isEmpty()) {
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(eventReminders);
            event.setReminders(reminders);
        }

        event = service.events().update(calendarId, event.getId(), event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

    public static void cancelAnInstance(String calendarId, String eventId, String instanceId) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        // First retrieve the instances from the API.
        Events instances = service.events().instances(calendarId, eventId).execute();

        // Select the instance to cancel.
        Event instance = instances.getItems().stream().filter(item -> item.getId().equals(instanceId)).findFirst().orElse(null);
        instance.setStatus("cancelled");

        Event updatedInstance = service.events().update(calendarId, instance.getId(), instance).execute();

        // Print the updated date.
        System.out.println(updatedInstance.getStatus());
        System.out.println(updatedInstance.getUpdated());
    }

    public static List<Event> getAllInstances(String calendarId, String eventId) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        // Iterate over the instances of a specific recurring event
        List<Event> allEvents = new ArrayList<>();
        String pageToken = null;
        do {
            Events events = service.events().instances(calendarId, eventId).setPageToken(pageToken).execute();
            List<Event> items = events.getItems();
            allEvents.addAll(items);
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
        return allEvents;
    }

    public static Events getInstances(String calendarId, String eventId, String pageToken, int pageSize) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        // Iterate over the instances of a specific recurring event
        return service.events().instances(calendarId, eventId).setPageToken(pageToken).setMaxResults(pageSize).execute();
    }

    public static void deleteEvent(String calendarId, String eventId) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        service.events().delete(calendarId, eventId).execute();
    }

    public static void updateEvent(String calendarId, String eventId, EventQdo eventQdo) throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        if (service == null) {
            return;
        }
        Event event = service.events().get(calendarId, eventId).execute();
        if (event == null) {
            event = new Event();
        }
        event.setSummary(eventQdo.summary)
                .setLocation(eventQdo.location)
                .setDescription(eventQdo.description);

        DateTime startDateTime = new DateTime(eventQdo.startDate);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setStart(start);

        DateTime endDateTime = new DateTime(eventQdo.endDate);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setEnd(end);

        if (eventQdo.recurringQdo != null) {
            List<String> recurrences = new ArrayList<>();
            Date endDate = eventQdo.recurringQdo.endTime;
            String rRule = "RRULE:FREQ=" + eventQdo.recurringQdo.frequency + ";UNTIL=" + dateFormat.format(endDate) + "Z";
            System.out.println(rRule);
            recurrences.add(rRule);
            event.setRecurrence(recurrences);
        }

        List<EventReminder> eventReminders = new ArrayList<>();
        if (eventQdo.sendEmailReminder) {
            eventReminders.add(new EventReminder().setMethod("email").setMinutes(eventQdo.emailReminderBeforeMinutes));
        }
        if (eventQdo.sendPopupReminder) {
            eventReminders.add(new EventReminder().setMethod("popup").setMinutes(eventQdo.popupReminderBeforeMinutes));
        }
        if (!eventReminders.isEmpty()) {
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(eventReminders);
            event.setReminders(reminders);
        }

        event = service.events().update(calendarId, event.getId(), event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

    public static void createEvent(EventQdo eventQdo) {
        Event event = new Event()
                .setSummary(eventQdo.summary)
                .setLocation(eventQdo.location)
                .setDescription(eventQdo.description);

        DateTime startDateTime = new DateTime(eventQdo.startDate, TimeZone.getTimeZone(eventQdo.timeZone));
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setStart(start);

        DateTime endDateTime = new DateTime(eventQdo.endDate, TimeZone.getTimeZone(eventQdo.timeZone));
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(eventQdo.timeZone);
        event.setEnd(end);

        if (eventQdo.recurringQdo != null) {
            List<String> recurrences = new ArrayList<>();
            Date endDate = eventQdo.recurringQdo.endTime;
            String rRule = "RRULE:FREQ=" + eventQdo.recurringQdo.frequency + ";UNTIL=" + dateFormat.format(endDate) + "Z;";
            rRule += "BYDAY=MO,TU,WE,TH,FR,SU";
            System.out.println(rRule);
            recurrences.add(rRule);
            event.setRecurrence(recurrences);
        }

        List<EventReminder> eventReminders = new ArrayList<>();
        if (eventQdo.sendEmailReminder) {
            eventReminders.add(new EventReminder().setMethod("email").setMinutes(eventQdo.emailReminderBeforeMinutes));
        }
        if (eventQdo.sendPopupReminder) {
            eventReminders.add(new EventReminder().setMethod("popup").setMinutes(eventQdo.popupReminderBeforeMinutes));
        }
        if (!eventReminders.isEmpty()) {
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(eventReminders);
            event.setReminders(reminders);
        }

        String calendarId = "primary";
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        try {
            event = service.events().insert(calendarId, event).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Event Id: "+event.getId());
    }

    public static void getEvents(Calendar service) throws IOException {
        // List the next 10 events from the primary calendar.
        DateTime now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(100)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s %s (%s)\n", event.getId(), event.getSummary(), start);
            }
        }
    }

    public static SignUpUserDetails getAccessTokenFromCode(String accessCode) {
        try {
            InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Load client secrets.
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    httpTransport,
                    JSON_FACTORY,
                    "https://oauth2.googleapis.com/token",
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    accessCode,
                    "http://localhost:4200").execute();

            GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();

            SignUpUserDetails signUpUserDetails = new SignUpUserDetails();
            signUpUserDetails.name = (String) payload.get("name");
            signUpUserDetails.email = payload.getEmail();
            signUpUserDetails.pictureUrl = (String) payload.get("picture");
            signUpUserDetails.accessToken = tokenResponse.getAccessToken();
            signUpUserDetails.refreshToken = tokenResponse.getRefreshToken();

            return signUpUserDetails;
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }

    public static Calendar getCalenderServiceFromRefreshToken(String refreshToken) {
        try {
            InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // Load client secrets.
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(httpTransport, JSON_FACTORY, refreshToken,
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret()).execute();

            GoogleCredential googleCredential = new GoogleCredential().setAccessToken(tokenResponse.getAccessToken());

            return new Calendar.Builder(httpTransport, JSON_FACTORY, googleCredential).setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public com.google.api.services.calendar.model.Calendar getPrimaryCalendar() throws IOException {
        Calendar service = getCalenderServiceFromRefreshToken(REFRESH_TOKEN);
        com.google.api.services.calendar.model.Calendar primary = service.calendars().get("primary").execute();
        System.out.println(primary.getId());
        System.out.println(primary.getTimeZone());
        return primary;
    }
}