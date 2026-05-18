// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

import java.util.List;

// Base type for every technical-analysis indicator.
// A source tag (e.g. "Binance", "Yahoo") lets each concrete factory stamp
// its indicators so the same SMA(200) coming from different sources is
// distinguishable in logs and dashboards.
public abstract class Indicator {

    protected final int    period;
    protected final String sourceTag;

    protected Indicator(int period, String sourceTag) {
        this.period    = period;
        this.sourceTag = sourceTag == null ? "" : sourceTag;
    }

    public abstract double calculate(List<PriceData> data);

    public abstract String getName();

    protected String tagPrefix() {
        return sourceTag.isEmpty() ? "" : sourceTag + "-";
    }
}

// Simple Moving Average:  SMA = (P1 + P2 + ... + Pn) / n
// Uses the last `period` close prices.
class SMA extends Indicator {

    public SMA(int period, String sourceTag) {
        super(period, sourceTag);
    }

    @Override
    public double calculate(List<PriceData> data) {
        if (data == null) {
            throw new IllegalArgumentException("Price list cannot be null.");
        }
        if (data.size() < period) {
            throw new IllegalArgumentException(
                    "Not enough price data for the selected period.");
        }

        double sum = 0;
        for (int i = data.size() - period; i < data.size(); i++) {
            sum += data.get(i).getClose();
        }
        return sum / period;
    }

    @Override
    public String getName() {
        return tagPrefix() + "SMA(" + period + ")";
    }
}

// Average True Range — a volatility measure.
// True Range for a bar is max of:
//   (high - low),  |high - prevClose|,  |low - prevClose|
// ATR is the simple average of the last `period` True Range values.
class ATR extends Indicator {

    public ATR(int period, String sourceTag) {
        super(period, sourceTag);
    }

    @Override
    public double calculate(List<PriceData> data) {
        if (data == null || data.size() < period + 1) {
            throw new IllegalArgumentException(
                    "Not enough data for ATR (need period + 1 rows).");
        }

        double sum = 0;
        int start = data.size() - period;
        for (int i = start; i < data.size(); i++) {
            PriceData curr = data.get(i);
            PriceData prev = data.get(i - 1);
            double tr = Math.max(
                    curr.getHigh() - curr.getLow(),
                    Math.max(
                            Math.abs(curr.getHigh() - prev.getClose()),
                            Math.abs(curr.getLow()  - prev.getClose())
                    )
            );
            sum += tr;
        }
        return sum / period;
    }

    @Override
    public String getName() {
        return tagPrefix() + "ATR(" + period + ")";
    }
}
