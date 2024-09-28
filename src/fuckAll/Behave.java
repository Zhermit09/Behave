package fuckAll;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Arrays;

public class Behave {

    public static int from = 22;
    public static int to = 6;
    public static int fromWeekend = 0;
    public static int toWeekend = 6;

    public static void load() {
        try {
            FileReader is = new FileReader("./timeFrame.txt");
            BufferedReader timeFrames = new BufferedReader(is);
            String line;
            while ((line = timeFrames.readLine()) != null) {
                String[] args = Arrays.stream(line.split("-")).filter(e -> !e.isEmpty()).toArray(String[]::new);
                for (String arg : args) {
                    String[] temp = arg.trim().split(" ");
                    switch (temp[0].trim()) {
                        case "f":
                            from = Integer.parseInt(temp[1].trim()) % 24;
                            break;
                        case "t":
                            to = Integer.parseInt(temp[1].trim()) % 24;
                            break;
                        case "fw":
                            fromWeekend = Integer.parseInt(temp[1].trim()) % 24;
                            break;
                        case "tw":
                            toWeekend = Integer.parseInt(temp[1].trim()) % 24;
                            break;
                    }
                }
            }
            timeFrames.close();
        } catch (Exception ignored) {
        }

    }

    public static void main(String[] args) throws Exception {
        while (true) {
            load();
            try (Socket socket = new Socket("time.nist.gov", 13); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String s;
                while ((s = in.readLine()) == null || s.isEmpty()) ;
                check((Integer.parseInt(s.substring(15, 17)) + 2) % 24);

            } catch (Exception e) {
                check(LocalTime.now().getHour());
            }
            Thread.sleep(10 * 60 * 1000);
        }
    }

    public static void check(int h) throws Exception {
        if (from > to && (from <= h || h <= to)) {
            kill();
        }
        if (from <= h && h <= to) {
            kill();
        }
    }

    public static void kill() throws Exception {
        Runtime.getRuntime().exec(new String[]{"shutdown -s -f -t 0"});
    }

}

