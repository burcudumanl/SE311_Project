import java.util.List;


public abstract class Indicator {
    protected int period;

    public Indicator(int period) {
        this.period = period;
    }

    public abstract double calculate(List<PriceData> data);

    public abstract String getName();
}


class SMA extends Indicator {

    public SMA(int period) {
        super(period);
    }

    @Override
    public double calculate(List<PriceData> data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Price list cannot be null."
            );
        }

        if (data.size() < period) {
            throw new IllegalArgumentException(
                    "Not enough price data for the selected period."
            );
        }

        double sum = 0;

        for (int i = data.size() - period; i < data.size(); i++) {
            sum += data.get(i).getClose();
        }

        return sum / period;
    }

    @Override
    public String getName() {
        return "SMA(" + period + ")";
    }
}

class ATR extends Indicator {

    public ATR(int period) {
        super(period);
    }

    @Override
    public double calculate(List<PriceData> data) {
        if (data == null || data.size() < period)
            throw new IllegalArgumentException("Not enough data for ATR.");
        double sum = 0;
        for (int i = data.size() - period; i < data.size(); i++)
            sum += (data.get(i).getHigh() - data.get(i).getLow());
        return sum / period;
    }

    @Override
    public String getName() {
        return "ATR(" + period + ")";
    }
}
