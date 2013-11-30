/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author Peter Lagerhem
 * Started: 2010-10-10
 *
 * History:
 * 2010-10-31 Fixed the sunset/sunrise calculation call.
 *
 */
package nu.nethome.home.items.timer;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import nu.nethome.home.item.ExecutionFailure;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * <h2>Purpose</h2>
 * To make it simple to assign intervals over days in several ways, with
 * possibilities for randomness and sun-rise and sun-set.
 * </p>
 * <p/>
 * <p>
 * <h2>Command syntax</h2>
 * A specific command format is used <i>per day field</i> with a syntax as
 * follows:<br/>
 * <br/>
 * <p/>
 * <code>
 * <b>
 * &lt;anytime-between&gt;[/[R/S +/- hour:min] - &lt;anytime-between&gt;[/[R/S +/- minutes]
 * </b>
 * </code>
 * </p>
 * <p/>
 * <p>
 * <code>&lt;anytime-between&gt;</code> can be expressed as a <i>timespan</i>
 * using the &lt; and &gt; characters, or as a <i>specific time:</i>.
 * </p>
 * <ul>
 * <li>
 * The <i>timespan</i> is expressed as &lt;hour:min-hour:min&gt;
 * (<i>startspan-endspan</i>) where hour is in the range of 00..23 and min in
 * the range of 00..59.<br/>
 * The span is a way for you to let the server pick a time for triggering a
 * command some time between the <i>startspan</i> and the <i>endspan</i>.<br/>
 * If either one time is missing, it gets the value of the other one.<br/>
 * If both are missing, the whole timespan is not used at all and no time is
 * therefore specified. See the comments for a detailed explanation.<br/>
 * If the <i>startspan</i> is later than the <i>endspan</i> it is assumed you
 * know what you are doing: you want to set the span between two days, such as
 * &lt;23:40-00:32&gt;.</li>
 * <li>
 * The <i>specific time</i> is expressed and &gt; which is the same as defining
 * a timespan with the same start and end times.<br/>
 * You may also omit the whole hour:min pair and only use the /R /S expression.</li>
 * </ul>
 * <p>
 * <code>/[ ...]</code> is an optional expression indicating sun-Rise (/R) or
 * sun-Set (/S) plus (+) or minus (-) 00..23 hours and 00..59 minutes
 * (mandatory)!.<br/>
 * You would use this to have the timer trigger depending on the sun state
 * instead - if reached earlier.<br/>
 * Internally, the current day of the year and the given location (long/lat)
 * returns the time of when the sun rises or sets.
 * </p>
 * <p/>
 * <p>
 * <h2>Examples:</h2>
 * <ul>
 * <li><code><06:45-08:00></code> defines a time between 06:45 and 08:00 (in the
 * morning) when the OnCommand will be triggered.</li>
 * <li><code>06:45</code> defines an exact time 06:45 when the OnCommand will be
 * triggered.</li>
 * <li><code>-<21:25-22:00></code> defines a time between 21:25 and 22:00 when
 * the OffCommand will be triggered. Note the leading - character.</li>
 * <li><code>-16:45</code> defines an exact time 16:45 when the OffCommand will
 * be triggered.</li>
 * <li><code><06:45-08:00>/[R+00:30]</code> defines a time sometime between
 * 06:45 and 08:00 (in the morning) or exactly when the sun rises+30 minutes if
 * it falls within the defined timespan, and will then trigger the OnCommand.</li>
 * <li><code><06:45>-<08:00>/[R+00:30]</code> The timer will trigger the
 * OnCommand exactly at 06:45, and trigger the OffCommand at 08:00 or earlier if
 * the sun has risen. Another way of expressing this is
 * <06:45-06:45>-<08:00-08:00>/[R+00:30]</li>
 * <li><code>[R-02:00]-09:00/[R]</code> defines a time 2 hours prior to when the
 * sun rises when the OnCommand will be triggered and a time no later than 09:00
 * or as soon as the sun has risen when the OffCommand is triggered.</li>
 * <li><code><06:45-07:00>-<08:00>/[R+00:30]</code> The timer will trigger the
 * OnCommand sometime between 06:45 and 07:00, and trigger the OffCommand at
 * 08:00 or earlier if the sun has risen.</li>
 * <li><code><15:00-16:00>/[S+00:30]-<23:30-01:00></code> The timer will trigger
 * the OnCommand sometime between 15:00 and 16:00 or as soon as the sun has set
 * + 30 minutes, and trigger the OffCommand sometime between 23:30 or no later
 * than 01:00.</li>
 * </ul>
 * <p/>
 * <p>
 * <h2>Comments:</h2>
 * An empty &lt;&gt; is pretty useless. Instead just leave it out. In this
 * scenario you can experiment with only triggering either one of the On or Off
 * commands.<br>
 * This is accomplished by defining the time as in the examples above, by
 * leaving the start or end out.
 * </p>
 */
public class FlexableAlarm {

    private static Logger logger = Logger.getLogger(FlexableAlarm.class
            .getName());

    /**
     * The AlarmItem class is an inner public class that exposes one alarm
     * definition parsed by the outer FlexableAlarm class.
     *
     * @see FlexableAlarm
     */
    public class AlarmItem {

        /**
         * A boolean that is 'true' if the alarm item is considered valid.
         */
        public boolean AlarmStatus = false;
        private Calendar dateEnd = null;
        private Calendar dateStart = null;
        private String endTime;
        private String fromCommand;
        private String fromOverride;
        private String fromTimeSpanEnd;
        private String fromTimeSpanStart;

        private boolean isCommand;
        private Boolean isFromOverride;

        private Boolean isfromTimeSpanEnd;
        private Boolean isfromTimeSpanStart;

        private boolean isTimespan;
        private Boolean isToOverride;

        private Boolean istoTimeSpanEnd;
        private Boolean istoTimeSpanStart;
        private String startTime;
        private String sunRiseSetOffset_1;
        private String sunRiseSetOffset_2;
        private String m_sunRiseString;
        private String m_sunSetString;
        private Calendar timerEnd = null;
        private Calendar timerStart = null;
        private String toCommand;

        private String toOverride;
        private String toTimeSpanEnd;

        private String toTimeSpanStart;

        AlarmItem() {

        }

        private Calendar calcSunRiseOrSunSet(String fromCommand, String offset)
                throws Exception {

            String calcTime = null;
            String ll[] = offset.split(":");
            if (ll.length != 2) {
                ll[0] = "0";
                ll[1] = "0";
            }
            int hourOffset = 0;
            int minuteOffset = 0;
            if (ll[0].charAt(0) == '+') {
                ll[0] = ll[0].substring(1);
                hourOffset = Integer.parseInt(ll[0]);
                minuteOffset = Integer.parseInt(ll[1]);
            }
            if (ll[0].charAt(0) == '-') {
                ll[0] = ll[0].substring(1);
                hourOffset = -Integer.parseInt(ll[0]);
                minuteOffset = -Integer.parseInt(ll[1]);
            }

            Calendar date = Calendar.getInstance();

            if (fromCommand.compareToIgnoreCase("S") == 0) {
                calcTime = m_sunSetString;
            } else if (fromCommand.compareToIgnoreCase("R") == 0) {
                calcTime = m_sunRiseString;
            } else {
                // Returns the current day/time instead
                return date;
            }

            Date parsedDate = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                parsedDate = format.parse(calcTime);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Calendar newDate = Calendar.getInstance();
            newDate.setTime(parsedDate);
            date.set(Calendar.HOUR_OF_DAY, newDate.get(Calendar.HOUR_OF_DAY));
            date.set(Calendar.MINUTE, newDate.get(Calendar.MINUTE));
            // System.out.println("Original calculated time = " +
            // date.getTime());
            date.add(Calendar.HOUR_OF_DAY, hourOffset);
            date.add(Calendar.MINUTE, minuteOffset);
            // System.out.println("Updated calculated time = " +
            // date.getTime());

            return date;
        }

        private String defaultTo(String p, String d) {
            return (p == null ? d : p);
        }

        /**
         * @return Date - time of day when the time period ends.<br/>
         *         The time returned is a Date object initialized as the current
         *         day and adjusted to the ending time of the alarm item.
         */
        public Date endsAtDate() {
            if (!isTimespan && !isCommand) {
                // These times are simple, formatted as: "07:00-17:00"
                return (isfromTimeSpanEnd ? stringToDate(fromTimeSpanEnd)
                        : null);
            }
            return (timerEnd != null ? calendarToDate(timerEnd) : null);
        }

        /**
         * @return Calendar - time of day when the time period ends.<br/>
         *         The time returned is a Calendar object initialized as the
         *         current day and adjusted to the ending time of the alarm
         *         item.
         */
        public Calendar endsAtCalendar() {
            return dateToCalendar(endsAtDate());
        }

        /**
         * @return String - time of day when the time period ends.<br/>
         *         The time is expressed as "HH:mm", for example: "17:15".
         */
        public String endsAtString() {
            if (!isTimespan && !isCommand) {
                // These times are simple, formatted as: "07:00-17:00"
                return (isfromTimeSpanEnd ? fromTimeSpanEnd : null);
            }
            return (timerEnd != null ? calendarToString(timerEnd) : null);
        }

        /**
         * Returns the sun rise of this alarmItem expressed as "HH:mm".
         *
         * @return string
         */
        public String getSunRiseString() {
            return m_sunRiseString;
        }

        /**
         * Returns the sun set of this alarmItem expressed as "HH:mm".
         *
         * @return the m_sunSetString
         */
        public String getSunSetString() {
            return m_sunSetString;
        }

        /**
         * This method parses one command according to the supported command
         * syntax.
         *
         * @param matcher
         */
        protected void parseCommand(Matcher matcher) {
            if (!hasValue(matcher.group(0)))
                return;
            String first = matcher.group(0);
            isTimespan = (first != null && first.length() > 0 && first.contains("<"));
            isCommand = (first != null && first.length() > 0 && first.contains("["));

            fromTimeSpanStart = matcher.group(3);
            isfromTimeSpanStart = hasValue(fromTimeSpanStart);
            fromTimeSpanEnd = matcher.group(7);
            isfromTimeSpanEnd = hasValue(fromTimeSpanEnd);
            toTimeSpanStart = matcher.group(18);
            istoTimeSpanStart = hasValue(toTimeSpanStart);
            toTimeSpanEnd = matcher.group(22);
            istoTimeSpanEnd = hasValue(toTimeSpanEnd);

            fromCommand = matcher.group(11);
            toCommand = matcher.group(26);

            sunRiseSetOffset_1 = defaultTo(matcher.group(12), "00:00");
            sunRiseSetOffset_2 = defaultTo(matcher.group(27), "00:00");

            startTime = matcher.group(2);
            endTime = matcher.group(17);

            fromOverride = matcher.group(10);
            isFromOverride = (fromOverride != null && fromOverride.length() > 1 && fromOverride
                    .charAt(0) == '/');
            toOverride = matcher.group(25);
            isToOverride = (toOverride != null && toOverride.length() > 1 && toOverride
                    .charAt(0) == '/');

            timerStart = null;
            timerEnd = null;

            // Alarm is ok.
            AlarmStatus = true;

            Calendar date = Calendar.getInstance();
            try {
                m_sunRiseString = getSunRiseStringByCalendar(date);
                m_sunSetString = getSunSetStringByCalendar(date);
            } catch (Exception e) {
                logger.info(e.getMessage());
                logger.info("Calculation of sunrise and sunset will be ignored");

                // Disable sunrise and sunset
                fromCommand = null;
                toCommand = null;
            }

            if (!isTimespan && !isCommand) {
                // These times are simple, formatted as: "07:00-17:00"
                // Check that times really are available, it is possible to enter: "9-21" which is not supported
                if (startTime.length() == 0)
                    AlarmStatus = false;
                return;
            }

            if (fromCommand != null) {
                if (internalDebug > 1)
                    System.out.println("Calculating Command");

                try {
                    dateStart = calcSunRiseOrSunSet(fromCommand,
                            sunRiseSetOffset_1);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (internalDebug > 1) {
                    System.out.format(
                            "Calc'd sunrise with offset: %s (offset by %s)\n",
                            DateFormat.getInstance()
                                    .format(dateStart.getTime()),
                            sunRiseSetOffset_1);
                }
            }

            // TODO:
            // Randomize between spans
            // Pick either randomized time or sunset/sunrise:
            // Pick sunrise, if sunrise comes before fromTimeSpans
            // Pick sunset, if sunset comes after toTimeSpans
            // if (fromOverride && dateStart

            if (toCommand != null) {
                try {
                    dateEnd = calcSunRiseOrSunSet(toCommand, sunRiseSetOffset_2);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (internalDebug > 1) {
                    System.out.format(
                            "Calc'd sunset with offset: %s (offset by %s)\n",
                            DateFormat.getInstance().format(dateEnd.getTime()),
                            sunRiseSetOffset_2);
                }
            }

            // }

            String calcSpanTimeFrom = null, calcSpanTimeTo = null;
            if (internalDebug > 1) {
                System.out.println("Calculating Timespan");
            }

            // Check for time(s)
            if (!isfromTimeSpanStart && isfromTimeSpanEnd) {
                isfromTimeSpanStart = true;
                fromTimeSpanStart = fromTimeSpanEnd;
            } else if (!isfromTimeSpanEnd && isfromTimeSpanStart) {
                isfromTimeSpanEnd = true;
                fromTimeSpanEnd = fromTimeSpanStart;
            }
            if (!istoTimeSpanStart && istoTimeSpanEnd) {
                istoTimeSpanStart = true;
                toTimeSpanStart = toTimeSpanEnd;
            } else if (!istoTimeSpanEnd && istoTimeSpanStart) {
                istoTimeSpanEnd = true;
                toTimeSpanEnd = toTimeSpanStart;
            }
            // System.out.format("From Span %s - %s\n", fromTimeSpanStart,
            // fromTimeSpanEnd);
            // System.out.format("To Span %s - %s\n", toTimeSpanStart,
            // toTimeSpanEnd);

            // fs1:fs2-fe1:fe2
            if (fromTimeSpanStart != null) {
                calcSpanTimeFrom = calcRandomTime(fromTimeSpanStart,
                        fromTimeSpanEnd);
                // System.out.format("Calc random time: %s\n",
                // calcSpanTimeFrom);
            }

            // ts1:ts2-te1:te2
            if (toTimeSpanStart != null) {
                calcSpanTimeTo = calcRandomTime(toTimeSpanStart,
                        toTimeSpanEnd);
                // System.out.format("Calc random time: %s\n",
                // calcSpanTimeTo);
            }

            // Calc a random time between in the span
            if (internalDebug > 1) {
                System.out.format("Random start set to : %s\n",
                        calcSpanTimeFrom);
                System.out.format("Random end set to : %s\n",
                        calcSpanTimeTo);
            }
            /*
                    * } else {
                    *
                    * if (internalDebug) { System.out.format("unknown command\n");
                    * } AlarmStatus = false; return;
                    */

            // Given an optional random time between timespans, and/or an
            // optional
            // sunrise/sunset (with offset)
            // it's now time to make a choice.
            if (calcSpanTimeFrom != null && dateStart != null) {
                // If the dateStart falls within the from timespan, use it
                // instead
                Calendar from = stringToCalendar(fromTimeSpanStart);
                Calendar to = stringToCalendar(fromTimeSpanEnd);

                if (dateStart.after(from) && dateStart.before(to))
                    timerStart = dateStart;
                else
                    timerStart = stringToCalendar(calcSpanTimeFrom);
            } else if (calcSpanTimeFrom != null && dateStart == null) {
                timerStart = stringToCalendar(calcSpanTimeFrom);
            } else if (calcSpanTimeFrom == null) {
                timerStart = dateStart;
                if (timerStart == null)
                    timerStart = stringToCalendar(startTime);
            }

            if (calcSpanTimeTo != null && dateEnd != null) {
                // If the dateEnd falls within the to timespan, use it
                // instead
                Calendar from = stringToCalendar(toTimeSpanStart);
                Calendar to = stringToCalendar(toTimeSpanEnd);

                if (dateEnd.after(from) && dateEnd.before(to))
                    timerEnd = dateEnd;
                else
                    timerEnd = stringToCalendar(calcSpanTimeTo);
            } else if (calcSpanTimeTo != null && dateEnd == null) {
                timerEnd = stringToCalendar(calcSpanTimeTo);
            } else if (calcSpanTimeTo == null) {
                timerEnd = dateEnd;
                if (timerEnd == null)
                    timerEnd = stringToCalendar(endTime);
            }
            /*
                * if (internalDebug) {
                * System.out.format("Command will be STARTED at: %02d:%02d\n",
                * timerStart.get(Calendar.HOUR_OF_DAY),
                * timerStart.get(Calendar.MINUTE));
                * System.out.format("Command will be STOPPED at: %02d:%02d\n",
                * timerEnd.get(Calendar.HOUR_OF_DAY),
                * timerEnd.get(Calendar.MINUTE)); }
                */
        }

        /**
         * @return Date - time of day when the time period begins.<br/>
         *         The time returned is a Date object initialized as the current
         *         day and adjusted to the beginning time of the alarm item.
         */
        public Date startsAtDate() {
            if (!isTimespan && !isCommand) {
                // These times are simple, formatted as: "07:00-17:00"
                return (isfromTimeSpanStart ? stringToDate(fromTimeSpanStart)
                        : null);
            }
            return (timerStart != null ? calendarToDate(timerStart) : null);
        }

        /**
         * @return Calendar - time of day when the time period begins.<br/>
         *         The time returned is a Calendar object initialized as the
         *         current day and adjusted to the beginning time of the alarm
         *         item.
         */
        public Calendar startsAtCalendar() {
            return dateToCalendar(startsAtDate());
        }

        /**
         * @return String - time of day when the time period begins.<br/>
         *         The time is expressed as "HH:mm", for example: "17:15".
         */
        public String startsAtString() {
            if (!isTimespan && !isCommand) {
                // These times are simple, formatted as: "07:00-17:00"
                return (isfromTimeSpanStart ? fromTimeSpanStart : null);
            }
            return (timerStart != null ? calendarToString(timerStart) : null);
        }

    }

    /**
     * Returns the sun rise of a particular calendar date and returns a string expressed as "HH:mm".
     *
     * @param cal a Calendar instance, or null for retrieving the current date time.
     * @return String
     * @throws Exception
     */
    public String getSunRiseStringByCalendar(Calendar cal) throws ExecutionFailure {
        if (cal == null)
            cal = Calendar.getInstance();
        return getCalculator().getOfficialSunriseForDate(cal);
    }

    /**
     * Returns the sun set of a particular calendar date and returns a string expressed as "HH:mm".
     *
     * @param cal a Calendar instance, or null for retrieving the current date time.
     * @return String
     * @throws Exception
     */
    public String getSunSetStringByCalendar(Calendar cal) throws ExecutionFailure {
        if (cal == null)
            cal = Calendar.getInstance();
        return getCalculator().getOfficialSunsetForDate(cal);
    }

    /**
     * Converts a Calendar object to a Date object.
     *
     * @param c - a Calendar object
     * @return Date
     */
    public static Date calendarToDate(Calendar c) {
        return new Date(c.getTimeInMillis());
    }

    /**
     * Converts a Date object to a Calendar object.
     *
     * @param d - a Date object
     * @return Calendar
     */
    public static Calendar dateToCalendar(Date d) {
        if (d == null)
            return null;
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }

    public static Calendar stringToCalendar(String d) {
        Calendar c = Calendar.getInstance();
        String s1 = d.substring(0, 2);
        String s2 = d.substring(3, 5);
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(d.substring(0, 2)));
        c.set(Calendar.MINUTE, Integer.parseInt(d.substring(3, 5)));
        return c;
    }

    public static Date stringToDate(String d) {
        return stringToCalendar(d).getTime();
    }

    private SunriseSunsetCalculator m_calculator = null;

    private int internalDebug = 0;

    private String m_LatLong = null;

    private String m_timeZoneIdentifier = null;

    private String regpat = "(([<]?((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?)?([-]?((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?))?[>]?)?([/]?\\[(\\w)(([+,-])((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?)?)?\\])?[-]?([<]?((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?)?([-]?((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?))?[>]?)?([/]?\\[(\\w)(([+,-])((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?)?)?\\])?)|((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?)?([-]?((0[0-9]|1[0-9]|2[0-3])?:([0-5][0-9])?))?";

    /*
      * Constructs the FlexableAlarm object.
      *
      * @param String longitudeLatitude - A string expressing a world coordinate
      * where alarm items are to be used at.
      */
    public FlexableAlarm(String longitudeLatitude, String timezoneIdentifier) {
        setLongitudeAndLatitude(longitudeLatitude);
        setTimeZoneIdentifier(timezoneIdentifier);
    }

    private String calcRandomTime(String fromTime, String toTime) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();

        calendar1.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(fromTime.substring(0, 2)));
        calendar1.set(Calendar.MINUTE,
                Integer.parseInt(fromTime.substring(3, 5)));
        calendar2.set(Calendar.HOUR_OF_DAY,
                Integer.parseInt(toTime.substring(0, 2)));
        calendar2
                .set(Calendar.MINUTE, Integer.parseInt(toTime.substring(3, 5)));

        long minutes1 = calendar1.getTimeInMillis();
        long minutes2 = calendar2.getTimeInMillis();
        long span_minutes = Math.abs(minutes2 - minutes1);
        if (minutes1 > minutes2) {
            // System.out.println(String.format("%02d - %02d -> %d", minutes1,
            // minutes2, minutes2 - minutes1));
            minutes2 += (60 * 24 * 60 * 1000);
            span_minutes = Math.abs(minutes2 - minutes1);
        }
        // System.out.println(String.format("%02d - %02d -> %d", minutes1,
        // minutes2, span_minutes));

        Random generator = new Random(minutes1);

        long newtime = ((long) (generator.nextDouble() * span_minutes) + Math
                .min(minutes2, minutes1));
        calendar2.setTimeInMillis(newtime);

        return String.format("%02d:%02d", calendar2.get(Calendar.HOUR_OF_DAY),
                calendar2.get(Calendar.MINUTE));
    }

    /**
     * Returns a string formatted as HH:mm for a Calendar object.
     *
     * @param c - a Calendar object
     * @return String
     */
    public static String calendarToString(Calendar c) {
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE));
    }

    private void createCalculator() throws ExecutionFailure {
        if (m_LatLong == null || m_LatLong.trim().length() == 0)
            throw new ExecutionFailure("Missing latitude and longitude");
        if (m_timeZoneIdentifier == null || m_timeZoneIdentifier.trim().length() == 0)
            throw new ExecutionFailure("Missing time zone identifier");

        String ll[] = m_LatLong.split(",");
        if (ll.length != 2)
            throw new ExecutionFailure("Illegal lat/long format, use 'long,lat'");

        ll[0] = ll[0].trim();
        ll[1] = ll[1].trim();

        // Location of sunrise/set, as latitude/longitude.
        Location location = new Location(ll[0], ll[1]);
        // Create m_calculator object with the location and time zone
        // identifier.
        m_calculator = new SunriseSunsetCalculator(location,
                m_timeZoneIdentifier);
    }

    protected SunriseSunsetCalculator getCalculator() throws ExecutionFailure {
        if (m_calculator == null)
            createCalculator();
        return m_calculator;
    }

    /**
     * @return the latLong
     */
    public String getLatitudeAndLongitude() {
        return m_LatLong;
    }

    /**
     * @return the m_timeZoneIdentifier
     */
    public String getTimeZoneIdentifier() {
        return m_timeZoneIdentifier;
    }

    private Boolean hasValue(String p) {
        return (p != null && p.length() > 0);
    }

    /**
     * Parse a string of command as is supported by this class. Several alarm
     * specific commands may be provided, each separated by the comma character.
     *
     * @param cmd
     * @return a list of AlarmItems successfully created by this method.
     */
    public List<AlarmItem> parseCommand(String cmd) {

        List<AlarmItem> alarmItems = new ArrayList<AlarmItem>();
        List<String> result;
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(regpat);
        matcher = pattern.matcher(cmd);
        result = new ArrayList<String>();

        if (internalDebug > 0) {
            System.out.println("---------------------------------------------");
            System.out.format("Parsing command '%s'\n", cmd);
        }

        while (matcher.find()) {
            if (internalDebug > 1) {
                System.out
                        .format("Start index: %d, End index: %d, groupcount: %d, group: '%s'\n",
                                matcher.start(), matcher.end(),
                                matcher.groupCount(), matcher.group());
            }

            if (matcher.groupCount() > 0 && hasValue(matcher.group(0))) {
                if (internalDebug > 1) {
                    for (int i = 0; i < matcher.groupCount() - 1; i++) {
                        System.out.format("%d: %s\n", i, matcher.group(i));
                        result.add(matcher.group(i));
                    }
                }
                // Take the found command and parse it separately
                AlarmItem ai;
                try {
                    ai = parseOneCommand(matcher);
                    if (ai != null) {
                        alarmItems.add(ai);
                    } else {
                        System.out.println("No match found!");
                    }
                } catch (StringIndexOutOfBoundsException e) {
                    // throw new IllegalFormat("Format is not correct");
                    break;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        if (internalDebug > 0) {
            if (alarmItems.size() == 0)
                System.out.println("No command(s) found.");

            for (int i = 0; i < alarmItems.size(); i++) {
                AlarmItem ai = alarmItems.get(i);
                String startDate = ai.startsAtString();
                System.out.format("Sun rise: %s (null if not calculated)\n",
                        ai.getSunRiseString());
                System.out.format("Sun sets: %s (null if not calculated)\n",
                        ai.getSunSetString());
                if (startDate != null)
                    System.out.format("* Command %d will be STARTED at: %s\n",
                            i + 1, startDate);
                else
                    System.out.format(
                            "* No START time defined for command %d\n", i + 1);
                String endDate = ai.endsAtString();
                if (endDate != null)
                    System.out.format("* Command %d will be STOPPED at: %s\n",
                            i + 1, endDate);
                else
                    System.out.format(
                            "* No STOP time defined for command %d\n", i + 1);
            }
        }

        return alarmItems;
    }

    private AlarmItem parseOneCommand(Matcher matcher) throws Exception {
        AlarmItem alarmItem = new AlarmItem();
        alarmItem.parseCommand(matcher);
        if (alarmItem.AlarmStatus)
            return alarmItem;
        return null;
    }

    /**
     * @param internalDebug the internalDebug to set
     */
    public void setInternalDebug(int internalDebug) {
        this.internalDebug = internalDebug;
    }

    /**
     * Sets a new position (latitude and longitude). The format expected is a string "latitude,longitude".
     *
     * @param longLat the latLong to set
     */
    public void setLongitudeAndLatitude(String longLat) {
        // Dispose the calculator, needs to be recalculated depending on the position.
        m_calculator = null;
        this.m_LatLong = longLat;
    }

    /**
     * @param timeZoneIdentifier the m_timeZoneIdentifier to set
     */
    public void setTimeZoneIdentifier(String timeZoneIdentifier) {
        m_calculator = null;
        this.m_timeZoneIdentifier = timeZoneIdentifier;
    }

}
