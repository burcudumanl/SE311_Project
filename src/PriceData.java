// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

import java.time.Instant;

// Domain model.
// Immutable price record shared across the data layer. Adapters build
// PriceData objects from CSV rows or JSON objects so the rest of the
// system never has to care about the original format.
public class PriceData {

    private final String  symbol;
    private final Instant timestamp;
    private final double  open;
    private final double  high;
    private final double  low;
    private final double  close;
    private final long    volume;

    public PriceData(String symbol, Instant timestamp,
                     double open, double high, double low, double close,
                     long volume) {
        this.symbol    = symbol;
        this.timestamp = timestamp;
        this.open      = open;
        this.high      = high;
        this.low       = low;
        this.close     = close;
        this.volume    = volume;
    }

    public String  getSymbol()    { return symbol; }
    public Instant getTimestamp() { return timestamp; }
    public double  getOpen()      { return open; }
    public double  getHigh()      { return high; }
    public double  getLow()       { return low; }
    public double  getClose()     { return close; }
    public long    getVolume()    { return volume; }

    @Override
    public String toString() {
        return symbol + " | " + timestamp
                + " | open=" + open + " close=" + close + " volume=" + volume;
    }
}
