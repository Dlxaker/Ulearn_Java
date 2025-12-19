import database.DatabaseManager;
import model.SportFacility;
import parser.CsvParser;
import service.AnalyticsService;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        CsvParser parser = new CsvParser();
        AnalyticsService analytics = new AnalyticsService();

        db.initDatabase();
        List<CsvParser.RawData> raw = parser.parse("data.csv");

        Set<String> regionNames = raw.stream()
                .map(CsvParser.RawData::region)
                .filter(name -> name != null && name.length() > 2)
                .collect(Collectors.toSet());
        Map<String, Integer> regionMap = db.saveRegions(regionNames);

        List<SportFacility> facilities = raw.stream()
                .filter(r -> regionMap.containsKey(r.region()))
                .map(r -> new SportFacility(r.id(), r.name(), regionMap.get(r.region()), r.address(), r.date()))
                .collect(Collectors.toList());

        db.saveFacilities(facilities);

        analytics.runAnalysis();
    }
}