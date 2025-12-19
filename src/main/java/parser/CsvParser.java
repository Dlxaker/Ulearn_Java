package parser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvParser {
    public record RawData(int id, String name, String region, String address, String date) {}

    public List<RawData> parse(String filePath) {
        List<RawData> data = new ArrayList<>();
        try {
            String content = Files.readString(Paths.get(filePath));
            Pattern p = Pattern.compile("(?m)^(\\d+),\"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\"");
            Matcher m = p.matcher(content);

            while (m.find()) {
                try {
                    int id = Integer.parseInt(m.group(1));
                    String name = m.group(2).replace("\n", " ").trim();
                    String region = m.group(3).replace("\n", " ").trim();
                    String address = m.group(4).replace("\n", " ").trim();
                    String date = m.group(5).trim();
                    data.add(new RawData(id, name, region, address, date));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }
}