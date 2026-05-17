import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ===== Adapter Pattern =====
// Common interface for every data format. Each nested class adapts a raw
// source (CSV, JSON, ...) into a unified List<PriceData>.
public interface DataSource {
    List<PriceData> fetch();
    String getName();
    // ----- Adapter: CSV / Tabular -----
    class Tabular implements DataSource {
        private String filePath;
        private String symbol;
        public Tabular(String filePath, String symbol) {
            this.filePath = filePath;
            this.symbol = symbol;
        }
        @Override
        public List<PriceData> fetch() {
            List<PriceData> result = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line = reader.readLine();
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    PriceData data = parseLine(line);
                    if (data != null) {
                        result.add(data);
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not read file: " + filePath + " (" + e.getMessage() + ")");
            }
            return result;
        }
        private PriceData parseLine(String line) {
            String[] parts = line.split(",");
            if (parts.length < 6) {
                System.err.println("Skipping bad row: " + line);
                return null;
            }
            try {
                String dateStr = parts[0].split("[ T]")[0];
                LocalDate date = LocalDate.parse(dateStr);
                double open  = Double.parseDouble(parts[1]);
                double high  = Double.parseDouble(parts[2]);
                double low   = Double.parseDouble(parts[3]);
                double close = Double.parseDouble(parts[4]);
                long volume = 0;
                for (int i = parts.length - 1; i >= 0; i--) {
                    try {
                        volume = Long.parseLong(parts[i]);
                        break;
                    } catch (NumberFormatException ignored) {}
                }
                return new PriceData(
                        symbol,
                        date.atStartOfDay().toInstant(ZoneOffset.UTC),
                        open, high, low, close, volume
                );
            } catch (Exception e) {
                System.err.println("Skipping unparseable row: " + line);
                return null;
            }
        }
        @Override
        public String getName() {
            return "Tabular/" + symbol;
        }
    }
    // ----- Adapter: JSON -----
    class Json implements DataSource {
        private String filePath;
        public Json(String filePath) {
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
}
