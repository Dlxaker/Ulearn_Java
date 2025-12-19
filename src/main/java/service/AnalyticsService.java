package service;

import database.DatabaseManager;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.File;
import java.sql.*;

public class AnalyticsService {
    private final DatabaseManager db = new DatabaseManager();

    public void runAnalysis() {
        try (Connection conn = db.connect()) {

            //Задание 2
            ResultSet rs1 = conn.createStatement().executeQuery(
                    "SELECT AVG(cnt) FROM (SELECT COUNT(*) as cnt FROM facilities GROUP BY region_id)");
            if (rs1.next()) System.out.printf("Задание 2:\n Среднее кол-во объектов в регионах: %.2f\n", rs1.getDouble(1));

            //Задание 3
            String top3Sql = "SELECT reg, SUM(c) as total FROM (" +
                    "SELECT CASE WHEN r.name LIKE '%Москва%' OR r.name LIKE '%Московская область%' " +
                    "THEN 'Москва и МО' ELSE r.name END as reg, COUNT(f.id) as c " +
                    "FROM facilities f JOIN regions r ON f.region_id = r.id GROUP BY r.name" +
                    ") GROUP BY reg ORDER BY total DESC LIMIT 3";

            ResultSet rs2 = conn.createStatement().executeQuery(top3Sql);
            System.out.println("Задание 3:\n ТОП-3 региона по количеству:");
            int i = 1;
            while (rs2.next()) System.out.println(i++ + ". " + rs2.getString(1) + " (" + rs2.getInt(2) + ")");

            createChart(conn);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createChart(Connection conn) throws Exception {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        //Задание 1
        String chartSql = "SELECT reg, SUM(c) as total FROM (" +
                "SELECT CASE WHEN r.name LIKE '%Москва%' OR r.name LIKE '%Московская область%' " +
                "THEN 'Москва и МО' ELSE r.name END as reg, COUNT(f.id) as c " +
                "FROM facilities f JOIN regions r ON f.region_id = r.id GROUP BY r.name" +
                ") GROUP BY reg HAVING total > 5 ORDER BY total DESC";

        ResultSet rs = conn.createStatement().executeQuery(chartSql);
        while (rs.next()) {
            String name = rs.getString("reg");
            int count = rs.getInt("total");
            if (name != null && !name.matches("\\d+")) {
                ds.addValue(count, "Объекты", name);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart("Спортивные объекты по регионам", "Регион", "Количество", ds);
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        ChartUtils.saveChartAsPNG(new File("Task_1_Chart.png"), chart, 1600, 900);
    }
}