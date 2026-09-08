import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HtmlReportManager {

    public static class TestResult {
        public String scenarioName;
        public String description;
        public String username;
        public String password;
        public String expectedResult;
        public String actualResult;
        public boolean passed;
        public long durationMs;
        public String screenshotFile;
        public String errorMessage;

        public TestResult(String scenarioName, String description, String username, String password,
                          String expectedResult, String actualResult, boolean passed, long durationMs,
                          String screenshotFile, String errorMessage) {
            this.scenarioName = scenarioName;
            this.description = description;
            this.username = username;
            this.password = password;
            this.expectedResult = expectedResult;
            this.actualResult = actualResult;
            this.passed = passed;
            this.durationMs = durationMs;
            this.screenshotFile = screenshotFile;
            this.errorMessage = errorMessage;
        }
    }

    private final List<TestResult> results = new ArrayList<>();
    private final String suiteName;
    private final String targetUrl;
    private long suiteStartTime;

    public HtmlReportManager(String suiteName, String targetUrl) {
        this.suiteName = suiteName;
        this.targetUrl = targetUrl;
        this.suiteStartTime = System.currentTimeMillis();
    }

    public void addResult(TestResult result) {
        results.add(result);
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void generateReport(String outputPath) {
        int totalTests = results.size();
        int passedTests = 0;
        for (TestResult r : results) {
            if (r.passed) passedTests++;
        }
        int failedTests = totalTests - passedTests;
        double passPercentage = totalTests > 0 ? ((double) passedTests / totalTests) * 100.0 : 0.0;
        long totalDurationMs = System.currentTimeMillis() - suiteStartTime;

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"UTF-8\">\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("  <title>Automation Test Report - ").append(escapeHtml(suiteName)).append("</title>\n")
            .append("  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n")
            .append("  <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n")
            .append("  <link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Fira+Code:wght@400;500&display=swap\" rel=\"stylesheet\">\n")
            .append("  <style>\n")
            .append("    :root {\n")
            .append("      --bg-color: #0b0f19;\n")
            .append("      --card-bg: rgba(17, 24, 39, 0.75);\n")
            .append("      --card-border: rgba(255, 255, 255, 0.08);\n")
            .append("      --primary: #3b82f6;\n")
            .append("      --primary-glow: rgba(59, 130, 246, 0.35);\n")
            .append("      --success: #10b981;\n")
            .append("      --success-glow: rgba(16, 185, 129, 0.25);\n")
            .append("      --danger: #ef4444;\n")
            .append("      --danger-glow: rgba(239, 68, 68, 0.25);\n")
            .append("      --text-main: #f9fafb;\n")
            .append("      --text-muted: #9ca3af;\n")
            .append("    }\n")
            .append("    * { box-sizing: border-box; margin: 0; padding: 0; }\n")
            .append("    body {\n")
            .append("      font-family: 'Outfit', sans-serif;\n")
            .append("      background-color: var(--bg-color);\n")
            .append("      background-image: \n")
            .append("        radial-gradient(at 10% 20%, rgba(59, 130, 246, 0.15) 0px, transparent 50%),\n")
            .append("        radial-gradient(at 90% 80%, rgba(139, 92, 246, 0.15) 0px, transparent 50%);\n")
            .append("      background-attachment: fixed;\n")
            .append("      color: var(--text-main);\n")
            .append("      min-height: 100vh;\n")
            .append("      padding: 2.5rem 1.5rem;\n")
            .append("    }\n")
            .append("    .container { max-width: 1200px; margin: 0 auto; }\n")
            .append("    header {\n")
            .append("      display: flex;\n")
            .append("      justify-content: space-between;\n")
            .append("      align-items: center;\n")
            .append("      margin-bottom: 2rem;\n")
            .append("      padding: 1.5rem 2rem;\n")
            .append("      background: var(--card-bg);\n")
            .append("      border: 1px solid var(--card-border);\n")
            .append("      backdrop-filter: blur(14px);\n")
            .append("      border-radius: 18px;\n")
            .append("      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);\n")
            .append("    }\n")
            .append("    .header-title h1 {\n")
            .append("      font-size: 1.8rem;\n")
            .append("      font-weight: 700;\n")
            .append("      background: linear-gradient(135deg, #ffffff 40%, var(--primary) 100%);\n")
            .append("      -webkit-background-clip: text;\n")
            .append("      -webkit-text-fill-color: transparent;\n")
            .append("    }\n")
            .append("    .header-title p { font-size: 0.95rem; color: var(--text-muted); margin-top: 0.35rem; }\n")
            .append("    .stats-grid {\n")
            .append("      display: grid;\n")
            .append("      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));\n")
            .append("      gap: 1.25rem;\n")
            .append("      margin-bottom: 2.5rem;\n")
            .append("    }\n")
            .append("    .stat-card {\n")
            .append("      background: var(--card-bg);\n")
            .append("      border: 1px solid var(--card-border);\n")
            .append("      backdrop-filter: blur(12px);\n")
            .append("      border-radius: 16px;\n")
            .append("      padding: 1.4rem;\n")
            .append("      text-align: center;\n")
            .append("      transition: transform 0.2s ease;\n")
            .append("    }\n")
            .append("    .stat-card:hover { transform: translateY(-4px); }\n")
            .append("    .stat-num {\n")
            .append("      font-size: 2.2rem;\n")
            .append("      font-weight: 800;\n")
            .append("      margin-top: 0.25rem;\n")
            .append("    }\n")
            .append("    .stat-label { font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); }\n")
            .append("    .color-total { color: #60a5fa; }\n")
            .append("    .color-pass { color: var(--success); }\n")
            .append("    .color-fail { color: var(--danger); }\n")
            .append("    .color-rate { color: #a78bfa; }\n")
            .append("    .section-title {\n")
            .append("      font-size: 1.4rem;\n")
            .append("      font-weight: 600;\n")
            .append("      margin-bottom: 1.25rem;\n")
            .append("      display: flex;\n")
            .append("      align-items: center;\n")
            .append("      gap: 0.5rem;\n")
            .append("    }\n")
            .append("    .test-card {\n")
            .append("      background: var(--card-bg);\n")
            .append("      border: 1px solid var(--card-border);\n")
            .append("      backdrop-filter: blur(12px);\n")
            .append("      border-radius: 16px;\n")
            .append("      padding: 1.5rem;\n")
            .append("      margin-bottom: 1.5rem;\n")
            .append("      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);\n")
            .append("    }\n")
            .append("    .test-header {\n")
            .append("      display: flex;\n")
            .append("      justify-content: space-between;\n")
            .append("      align-items: center;\n")
            .append("      border-bottom: 1px solid rgba(255, 255, 255, 0.06);\n")
            .append("      padding-bottom: 0.9rem;\n")
            .append("      margin-bottom: 1rem;\n")
            .append("    }\n")
            .append("    .test-title { font-size: 1.15rem; font-weight: 600; }\n")
            .append("    .badge {\n")
            .append("      padding: 0.35rem 0.9rem;\n")
            .append("      border-radius: 50px;\n")
            .append("      font-size: 0.8rem;\n")
            .append("      font-weight: 700;\n")
            .append("      letter-spacing: 0.05em;\n")
            .append("    }\n")
            .append("    .badge-pass { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid var(--success); }\n")
            .append("    .badge-fail { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid var(--danger); }\n")
            .append("    .test-grid {\n")
            .append("      display: grid;\n")
            .append("      grid-template-columns: 2fr 1fr;\n")
            .append("      gap: 1.5rem;\n")
            .append("    }\n")
            .append("    @media (max-width: 850px) { .test-grid { grid-template-columns: 1fr; } }\n")
            .append("    .meta-table { width: 100%; border-collapse: collapse; font-size: 0.92rem; }\n")
            .append("    .meta-table td { padding: 0.5rem 0; vertical-align: top; }\n")
            .append("    .meta-table td.label { width: 140px; color: var(--text-muted); font-weight: 500; }\n")
            .append("    .code-val { font-family: 'Fira Code', monospace; background: rgba(255, 255, 255, 0.06); padding: 2px 6px; border-radius: 4px; font-size: 0.85rem; }\n")
            .append("    .screenshot-box {\n")
            .append("      background: rgba(0, 0, 0, 0.3);\n")
            .append("      border: 1px solid var(--card-border);\n")
            .append("      border-radius: 12px;\n")
            .append("      padding: 0.75rem;\n")
            .append("      text-align: center;\n")
            .append("    }\n")
            .append("    .screenshot-img {\n")
            .append("      width: 100%;\n")
            .append("      max-height: 180px;\n")
            .append("      object-fit: cover;\n")
            .append("      border-radius: 8px;\n")
            .append("      cursor: pointer;\n")
            .append("      transition: transform 0.2s ease;\n")
            .append("      border: 1px solid rgba(255, 255, 255, 0.1);\n")
            .append("    }\n")
            .append("    .screenshot-img:hover { transform: scale(1.02); }\n")
            .append("    .screenshot-caption { font-size: 0.75rem; color: var(--text-muted); margin-top: 0.4rem; }\n")
            .append("    .error-box {\n")
            .append("      margin-top: 0.8rem;\n")
            .append("      padding: 0.75rem;\n")
            .append("      border-radius: 8px;\n")
            .append("      background: rgba(239, 68, 68, 0.1);\n")
            .append("      border: 1px solid rgba(239, 68, 68, 0.3);\n")
            .append("      color: #fca5a5;\n")
            .append("      font-family: 'Fira Code', monospace;\n")
            .append("      font-size: 0.82rem;\n")
            .append("    }\n")
            .append("    footer {\n")
            .append("      text-align: center;\n")
            .append("      margin-top: 3rem;\n")
            .append("      font-size: 0.85rem;\n")
            .append("      color: var(--text-muted);\n")
            .append("    }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <div class=\"container\">\n")
            .append("    <header>\n")
            .append("      <div class=\"header-title\">\n")
            .append("        <h1>").append(escapeHtml(suiteName)).append("</h1>\n")
            .append("        <p>Target Application: <span class=\"code-val\">").append(escapeHtml(targetUrl)).append("</span> | Executed on: ").append(formattedDate).append("</p>\n")
            .append("      </div>\n")
            .append("    </header>\n\n")
            .append("    <div class=\"stats-grid\">\n")
            .append("      <div class=\"stat-card\">\n")
            .append("        <div class=\"stat-label\">Total Scenarios</div>\n")
            .append("        <div class=\"stat-num color-total\">").append(totalTests).append("</div>\n")
            .append("      </div>\n")
            .append("      <div class=\"stat-card\">\n")
            .append("        <div class=\"stat-label\">Passed</div>\n")
            .append("        <div class=\"stat-num color-pass\">").append(passedTests).append("</div>\n")
            .append("      </div>\n")
            .append("      <div class=\"stat-card\">\n")
            .append("        <div class=\"stat-label\">Failed</div>\n")
            .append("        <div class=\"stat-num color-fail\">").append(failedTests).append("</div>\n")
            .append("      </div>\n")
            .append("      <div class=\"stat-card\">\n")
            .append("        <div class=\"stat-label\">Pass Rate</div>\n")
            .append("        <div class=\"stat-num color-rate\">").append(String.format("%.1f%%", passPercentage)).append("</div>\n")
            .append("      </div>\n")
            .append("    </div>\n\n")
            .append("    <h2 class=\"section-title\">Execution Details</h2>\n");

        for (int i = 0; i < results.size(); i++) {
            TestResult r = results.get(i);
            html.append("    <div class=\"test-card\">\n")
                .append("      <div class=\"test-header\">\n")
                .append("        <div class=\"test-title\">#").append(i + 1).append(". ").append(escapeHtml(r.scenarioName)).append("</div>\n")
                .append("        <div class=\"badge ").append(r.passed ? "badge-pass" : "badge-fail").append("\">")
                .append(r.passed ? "PASSED" : "FAILED").append("</div>\n")
                .append("      </div>\n")
                .append("      <div class=\"test-grid\">\n")
                .append("        <div>\n")
                .append("          <table class=\"meta-table\">\n")
                .append("            <tr><td class=\"label\">Description:</td><td>").append(escapeHtml(r.description)).append("</td></tr>\n")
                .append("            <tr><td class=\"label\">Username / Email:</td><td><span class=\"code-val\">").append(escapeHtml(r.username)).append("</span></td></tr>\n")
                .append("            <tr><td class=\"label\">Password:</td><td><span class=\"code-val\">").append(escapeHtml(r.password)).append("</span></td></tr>\n")
                .append("            <tr><td class=\"label\">Expected:</td><td>").append(escapeHtml(r.expectedResult)).append("</td></tr>\n")
                .append("            <tr><td class=\"label\">Actual:</td><td>").append(escapeHtml(r.actualResult)).append("</td></tr>\n")
                .append("            <tr><td class=\"label\">Duration:</td><td>").append(String.format("%.2fs", r.durationMs / 1000.0)).append("</td></tr>\n")
                .append("          </table>\n");

            if (r.errorMessage != null && !r.errorMessage.isEmpty()) {
                html.append("          <div class=\"error-box\"><strong>Error:</strong> ").append(escapeHtml(r.errorMessage)).append("</div>\n");
            }

            html.append("        </div>\n");

            if (r.screenshotFile != null && !r.screenshotFile.isEmpty()) {
                html.append("        <div class=\"screenshot-box\">\n")
                    .append("          <a href=\"").append(escapeHtml(r.screenshotFile)).append("\" target=\"_blank\">\n")
                    .append("            <img class=\"screenshot-img\" src=\"").append(escapeHtml(r.screenshotFile)).append("\" alt=\"Execution Screenshot\" />\n")
                    .append("          </a>\n")
                    .append("          <div class=\"screenshot-caption\">Click to view full screenshot</div>\n")
                    .append("        </div>\n");
            }

            html.append("      </div>\n")
                .append("    </div>\n");
        }

        html.append("    <footer>\n")
            .append("      <p>Automated Selenium Test Execution Report &bull; Total Suite Duration: ")
            .append(String.format("%.2fs", totalDurationMs / 1000.0)).append("</p>\n")
            .append("    </footer>\n")
            .append("  </div>\n")
            .append("</body>\n")
            .append("</html>\n");

        File outFile = new File(outputPath);
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(outFile, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(html.toString());
            System.out.println("[HTML REPORT GENERATED] Saved to: " + outFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to write HTML report: " + e.getMessage());
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
