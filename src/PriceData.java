import java.time.Instant;

public class PriceData {
    private String symbol;
    private Instant timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;

    public PriceData(String symbol, Instant timestamp, double open, double high,
                     double low, double close, long volume) {
        this.symbol = symbol;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public String getSymbol() { return symbol; }
    public Instant getTimestamp() { return timestamp; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public long getVolume() { return volume; }

    @Override
    public String toString() {
        return symbol + " | " + timestamp + " | open=" + open + " close=" + close + " volume=" + volume;
    }
}
