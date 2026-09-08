import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelReportManager {

    private static final String CSV_FILE_PATH = "test_report.csv";

    /**
     * Appends test execution results into the Excel CSV report file.
     * If the file doesn't exist, it creates it with header columns.
     * If it already exists (from earlier/daily runs), it appends new rows with current Date and Time.
     */
    public static synchronized void appendResultsToExcel(List<HtmlReportManager.TestResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }

        File file = new File(CSV_FILE_PATH);
        boolean fileExists = file.exists();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentRunTimestamp = LocalDateTime.now().format(dtf);

        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, true);
             PrintWriter pw = new PrintWriter(fw)) {

            // If new file, write UTF-8 BOM (for Excel) and Header row
            if (!fileExists || file.length() == 0) {
                // UTF-8 BOM so Excel opens with proper encoding
                fw.write('\ufeff');
                pw.println("Date & Time,Scenario Name,Test Description,Tested Username / Email,Tested Password,Expected Result,Actual Result,Status,Duration (sec),Screenshot,Error Details");
            }

            // Append each scenario result
            for (HtmlReportManager.TestResult r : results) {
                if (r == null) continue;
                String sanitizedError = r.errorMessage != null ? r.errorMessage.replaceAll("[\\r\\n]+", " | ").trim() : "None";
                String row = String.format(
                        "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%.2f\",\"%s\",\"%s\"",
                        escapeCsv(currentRunTimestamp),
                        escapeCsv(r.scenarioName),
                        escapeCsv(r.description),
                        escapeCsv(r.username),
                        escapeCsv(r.password),
                        escapeCsv(r.expectedResult),
                        escapeCsv(r.actualResult),
                        r.passed ? "PASSED" : "FAILED",
                        r.durationMs / 1000.0,
                        escapeCsv(r.screenshotFile != null ? r.screenshotFile : "N/A"),
                        escapeCsv(sanitizedError)
                );
                pw.println(row);
            }

            System.out.println("[EXCEL REPORT UPDATED] Appended results with timestamp (" + currentRunTimestamp + ") to: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error writing to Excel report: " + e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
