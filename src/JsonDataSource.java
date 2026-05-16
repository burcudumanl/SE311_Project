import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonDataSource implements DataSource {

    private String filePath;

    public JsonDataSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<PriceData> fetch() {
        List<PriceData> result = new ArrayList<>();

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            Pattern objectPattern = Pattern.compile("\\{[^}]*\\}");
            Matcher matcher = objectPattern.matcher(content);

            while (matcher.find()) {
                String block = matcher.group();
                PriceData data = parseObject(block);
                if (data != null) {
                    result.add(data);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: " + filePath + " (" + e.getMessage() + ")");
        }

        return result;
    }

    private PriceData parseObject(String block) {
        try {
            String symbol     = readText(block, "symbol");
            String timestamp  = readText(block, "timestamp");
            double open       = readNumber(block, "open");
            double high       = readNumber(block, "high");
            double low        = readNumber(block, "low");
            double close      = readNumber(block, "close");
            long volume       = (long) readNumber(block, "volume");

            return new PriceData(
                    symbol,
                    Instant.parse(timestamp),
                    open, high, low, close, volume
            );
        } catch (Exception e) {
            System.err.println("Skipping bad JSON object: " + block);
            return null;
        }
    }

    private String readText(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(block);
        return m.find() ? m.group(1) : null;
    }

    private double readNumber(String block, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher m = p.matcher(block);
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    @Override
    public String getName() {
        return "JSON/" + filePath;
    }
}
