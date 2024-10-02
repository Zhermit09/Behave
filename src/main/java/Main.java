import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Main {
    public static LocalTime from = LocalTime.of(22, 0);
    public static LocalTime to = LocalTime.of(6, 0);
    public static LocalTime fromW = LocalTime.of(0, 0);
    public static LocalTime toW = LocalTime.of(6, 0);
    static LocalDateTime time;

    public static void main(String[] args) {

        while (true) {
            load();
            try (Socket socket = new Socket("time.nist.gov", 13); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String responce;
                while ((responce = in.readLine()) == null || responce.isEmpty()) ;
                convertTime(responce.substring(6, 23));

            } catch (Exception e) {
                time = LocalDateTime.now();
            }

            DayOfWeek day;
            if (time.toLocalTime().isAfter(to.isAfter(toW) ? to : toW)) {
                day = time.getDayOfWeek();
            } else {
                day = time.getDayOfWeek().minus(1);
            }

            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                checkTime(fromW, toW);
            } else {
                checkTime(from, to);
            }
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (Exception ignore) {
            }
        }

    }

    public static void load() {
        try {
            URI uri = new URI("https://raw.githubusercontent.com/Zhermit09/Behave/refs/heads/main/src/main/resources/timeFrame.txt");
            BufferedReader timeFrames = new BufferedReader(new InputStreamReader(uri.toURL().openStream()));
            String line;
            while ((line = timeFrames.readLine()) != null) {
                String[] args = Arrays.stream(line.split("-")).filter(e -> !e.isEmpty()).toArray(String[]::new);
                for (String arg : args) {
                    String[] temp = arg.trim().split(" ");
                    String[] time = temp[1].trim().split(":");
                    int h = Integer.parseInt(time[0]) % 24;
                    int m = Integer.parseInt(time[1]) % 60;
                    switch (temp[0].trim()) {
                        case "f":
                            from = LocalTime.of(h, m);
                            break;
                        case "t":
                            to = LocalTime.of(h, m);
                            break;
                        case "fw":
                            fromW = LocalTime.of(h, m);
                            break;
                        case "tw":
                            toW = LocalTime.of(h, m);
                            break;
                    }
                }
            }
            timeFrames.close();
        } catch (Exception ignored) {
        }
    }

    public static void convertTime(String utcTimeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
        LocalDateTime utcDateTime = LocalDateTime.parse(utcTimeString, formatter);

        ZonedDateTime utcZonedDateTime = utcDateTime.atZone(ZoneId.of("UTC"));
        time = utcZonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Stockholm")).toLocalDateTime();
    }

    public static void checkTime(LocalTime start, LocalTime end) {
        LocalTime curr = time.toLocalTime();
        if (start.isAfter(end) && (start.isBefore(curr) || curr.isBefore(end))) {
            kill();
        }
        if (start.isBefore(curr) && curr.isBefore(end)) {
            kill();
        }
    }

    public static void kill() {
        try {
            Runtime.getRuntime().exec(new String[]{/*"shutdown", "-s", "-f", "-t", "0" /**/  "notepad.exe"/**/});
        } catch (Exception ignored) {
            kill();
        }
    }
}
