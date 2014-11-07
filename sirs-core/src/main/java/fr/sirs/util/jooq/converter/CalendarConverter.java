package fr.sirs.util.jooq.converter;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jooq.Converter;

public class CalendarConverter implements Converter<Timestamp, Calendar> {

    /**
     * Serialization UID
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Calendar from(Timestamp databaseObject) {
        GregorianCalendar calendar = (GregorianCalendar) GregorianCalendar.getInstance();
        calendar.setTimeInMillis(databaseObject.getTime());
        return calendar;
    }

    @Override
    public Timestamp to(Calendar userObject) {
        return new Timestamp(userObject.getTime().getTime());
    }

    @Override
    public Class<Timestamp> fromType() {
        return Timestamp.class;
    }

    @Override
    public Class<Calendar> toType() {
        return Calendar.class;
    }
}