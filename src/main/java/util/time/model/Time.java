package util.time.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.time.logic.TimeDeserializer;
import util.time.logic.TimeSerializer;

import java.sql.Timestamp;
import java.text.ParsePosition;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

@JsonDeserialize(using = TimeDeserializer.class)
@JsonSerialize(using = TimeSerializer.class)
public class Time{
    private Instant time;
    static String defaultStringPattern = "uuuu-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Time(){
        time = Instant.now();
    }

    public Time(Instant time){
        this.time = time;
    }

    public static Time parse(String time){
        Time new_time = parse(time, defaultStringPattern);
        return new_time;
    }

    public static Time parse(String time, String pattern){
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(pattern)
                .withZone(ZoneOffset.UTC);
        TemporalAccessor parse = formatter.parse(time, new ParsePosition(0));
        return new Time(LocalDateTime.from(parse).toInstant(ZoneOffset.UTC));
    }

    public String toString(){
        return toString(defaultStringPattern);
    }

    public String toString(String pattern){
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern(pattern)
                .withZone(ZoneOffset.UTC);
        return formatter.format( time );
    }

    public String mysqlString(){
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("uuuu-MM-dd HH:mm:ss.SSS")
                .withZone(ZoneOffset.UTC);
        return formatter.format( time );
    }

    public boolean isBefore(Time time){
        return this.time.isBefore(time.time);
    }

    public boolean isBeforeOrEqual(Time time){
        int comparison = this.getInstant().compareTo(time.getInstant());
        if (comparison <= 0) {
            return true;
        }
        return false;
    }

    public Instant getInstant(){
        return time;
    }

    public Time addMinutes(long minutes){
        return new Time(time.plusSeconds(minutes * 60L));
    }

    public Time addDays(long days){
        return new Time(time.plusSeconds(days * 24L * 60L * 60L));
    }

    public Time addSeconds(long seconds){
        return new Time(time.plusSeconds(seconds));
    }

    public Time toHourStart(){
        return new Time(this.time.truncatedTo(ChronoUnit.HOURS));
    }

    public Time toMinuteStart(){
        return new Time(this.time.truncatedTo(ChronoUnit.MINUTES));
    }

    public Time toSecondStart(){
        return new Time(this.time.truncatedTo(ChronoUnit.SECONDS));
    }

    public Time toDayStart(){
        return new Time(this.time.truncatedTo(ChronoUnit.DAYS));
    }

    public static Time now(){
        return new Time();
    }

    public static Instant toInstant(Time time){
        if (time == null){
            return null;
        } else {
            return time.getInstant();
        }
    }

    public static Timestamp toTimestamp(Time time) {
        if (time == null){
            return null;
        } else {
            return Timestamp.from(time.getInstant());
        }
    }

    public static Time fromTimestamp(Timestamp timestamp) {
        if (timestamp == null){
            return null;
        } else {
            return new Time(timestamp.toInstant());
        }
    }

    public Duration duration(Time time) {
        return Duration.between(this.time, time.getInstant());
    }

}
