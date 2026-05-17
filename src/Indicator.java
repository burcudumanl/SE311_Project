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
        if (data == null || data.size() < period || period <= 0) {
            return 0;
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
class EMA extends Indicator {

    public EMA(int period) {
        super(period);
    }
@Override
    public double calculate(List<PriceData> data) {
        if (data == null || data.size() < period || period <= 0) {
            return 0;
        }

        double multiplier = 2.0 / (period + 1);

        double ema = data.get(data.size() - period).getClose();

        for (int i = data.size() - period + 1; i < data.size(); i++) {
            double close = data.get(i).getClose();
            ema = (close - ema) * multiplier + ema;
        }

        return ema;
    }

    @Override
    public String getName() {
        return "EMA(" + period + ")";
    }}
    
