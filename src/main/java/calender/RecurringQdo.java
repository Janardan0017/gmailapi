package calender;

import java.util.Date;

/**
 * Created by emp350 on 04/03/21
 */
public class RecurringQdo {

    public Frequency frequency;

    public Date endTime;

    public int interval;

    public RecurringQdo() {
    }

    public RecurringQdo(Frequency frequency, Date endTime, int interval) {
        this.frequency = frequency;
        this.endTime = endTime;
        this.interval = interval;
    }
}
