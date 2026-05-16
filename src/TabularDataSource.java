import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TabularDataSource implements DataSource {

    private String filePath;
    private String symbol;

    public TabularDataSource(String filePath, String symbol) {
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
            LocalDate date = LocalDate.parse(parts[0]);
            double open  = Double.parseDouble(parts[1]);
            double high  = Double.parseDouble(parts[2]);
            double low   = Double.parseDouble(parts[3]);
            double close = Double.parseDouble(parts[4]);
            long volume  = Long.parseLong(parts[parts.length - 1]);

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
