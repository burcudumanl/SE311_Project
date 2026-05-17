import java.util.List;

// ===== Data Layer Demo =====
// Wires Observer + Adapter patterns together end-to-end.
public class DataLayerDemo {
    public static void main(String[] args) {
        printHeader("DATA LAYER DEMO");
        DataPublisher publisher = new DataPublisher();
        publisher.subscribe(new ConsoleObserver("Chart"));
        publisher.subscribe(new ConsoleObserver("ProfitLoss"));
        System.out.println("[Setup]");
        System.out.println("  Active observers: " + publisher.getObserverCount());
        int totalRecords = 0;
        System.out.println("----------------------------------------");
        totalRecords += runSource(
                new DataSource.Json("data/sample_feed.json"),
                publisher
        );
        System.out.println("----------------------------------------");
        totalRecords += runSource(
                new DataSource.Tabular("data/BTC-USD.csv", "BTC-USD"),
                publisher
        );
        System.out.println("----------------------------------------");
        totalRecords += runSource(
                new DataSource.Tabular("data/ETH-USD.csv", "ETH-USD"),
                publisher
        );
        System.out.println("----------------------------------------");
        System.out.println("[Summary]");
        System.out.println("  Records published: " + totalRecords);
        System.out.println("  Observer calls:    " + totalRecords * publisher.getObserverCount());
        printHeader("DEMO FINISHED");
    }
    private static int runSource(DataSource source, DataPublisher publisher) {
        System.out.println("[Source: " + source.getName() + "]");
        List<PriceData> rows = source.fetch();
        if (rows.isEmpty()) {
            System.out.println("  No data available — skipping");
            return 0;
        }
        System.out.println("  Loaded " + rows.size() + " records");
        int limit = Math.min(3, rows.size());
        for (int i = 0; i < limit; i++) {
            PriceData row = rows.get(i);
            System.out.printf("  #%d  %s  open=%.2f  close=%.2f  vol=%d%n",
                    i + 1, row.getSymbol(), row.getOpen(), row.getClose(), row.getVolume());
            publisher.publish(row);
        }
        if (rows.size() > limit) {
            System.out.println("  ... (" + (rows.size() - limit) + " more, not shown)");
        }
        return rows.size();
    }
    private static void printHeader(String title) {
        System.out.println("----- " + title + " -----");
    }
    // ===== Demo Observer =====
    private static class ConsoleObserver implements DataPublisher.Observer {
        private String name;
        public ConsoleObserver(String name) {
            this.name = name;
        }
        @Override
        public void onPriceUpdate(PriceData data) {
            System.out.printf("    -> %-12s got %s @ %.2f%n",
                    name, data.getSymbol(), data.getClose());
        }
    }
}
