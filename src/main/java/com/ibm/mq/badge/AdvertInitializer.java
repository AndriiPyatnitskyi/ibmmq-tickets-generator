package com.ibm.mq.badge;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ibm.mq.events.Advert;

@Getter
@Setter
public class AdvertInitializer {
    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");
    private static final String FILENAME = "Data.json";
    private final AtomicInteger uniqueID = new AtomicInteger(0);
    private HashMap<Integer, Advert> bookableAdverts = new HashMap<>();

    public AdvertInitializer() throws FileNotFoundException {
        initAdverts();
    }

    private void initAdverts() throws FileNotFoundException {
        getJsonArrayFromFile(FILENAME)
            .forEach(o -> {
                Advert advert = createEventFromJson((JSONObject) o);
                bookableAdverts.put(advert.getEventID(), advert);
            });
    }

    private Advert createEventFromJson(JSONObject eventObject) {
        Integer eventID = uniqueID.getAndIncrement();
        String name = getValueFromJson(eventObject, "Name");
        String location = getValueFromJson(eventObject, "Location");
        LocalTime time = getTimeFromString(getValueFromJson(eventObject, "Time"));
        LocalDate date = getDateFromString(getValueFromJson(eventObject, "Date"));
        Integer ticketQuantity = Integer.parseInt(getValueFromJson(eventObject, "Ticket Quantity"));

        return Advert.builder()
            .eventID(eventID)
            .title(name)
            .time(String.valueOf(time))
            .date(String.valueOf(date))
            .location(location)
            .capacity(ticketQuantity)
            .build();
    }

    private JSONArray getJsonArrayFromFile(String filename) throws FileNotFoundException {
        File file = new File(filename);

        Scanner scanner = new Scanner(file);

        String next = scanner
            .useDelimiter("\\Z")
            .next();

        return new JSONObject(next)
            .getJSONArray("Events");
    }

    private LocalDate getDateFromString(String stringHolder) {
        if (!isNull(stringHolder)) {
            try {
                return LocalDate.parse(stringHolder);
            } catch (DateTimeParseException e) {
                logger.warning("Date from data is not in a valid format");
                logger.warning(e.toString());
            }
        } else {
            logger.warning("Could not get date from null string");
        }
        return null;
    }

    private LocalTime getTimeFromString(String stringHolder) {
        LocalTime time = null;
        if (!isNull(stringHolder)) {
            try {
                time = LocalTime.parse(stringHolder);
            } catch (DateTimeParseException e) {
                logger.warning("Time from data is not in a valid format");
                logger.warning(e.toString());
            }
        } else {
            logger.warning("Could not get time from null string");
        }
        return time;
    }

    private String getValueFromJson(JSONObject holderObj, String key) {
        try {
            return String.valueOf(holderObj.get(key));
        } catch (JSONException e) {
            logger.warning("Was not able to get '" + key + "' from json");
            return null;
        }
    }

    private boolean isNull(String str) {
        return str == null || str.isEmpty();
    }
}