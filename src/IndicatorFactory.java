public abstract class IndicatorFactory {

    public abstract Indicator createSMA(int period);
    public abstract Indicator createATR(int period);
    public abstract RiskCalculator createRiskCalculator();

    public static IndicatorFactory getFactory(String source) {
        switch (source) {
            case "Binance": return new BinanceIndicatorFactory();
            case "Yahoo":   return new YahooIndicatorFactory();
            default: throw new IllegalArgumentException("Unknown source: " + source);
        }
    }

    static class BinanceIndicatorFactory extends IndicatorFactory {
        @Override
        public Indicator createSMA(int period) {
            System.out.println("[BinanceFactory] Creating SMA(" + period + ")");
            return new SMA(period);
        }
        @Override
        public Indicator createATR(int period) {
            System.out.println("[BinanceFactory] Creating ATR(" + period + ")");
            return new ATR(period);
        }
        @Override
        public RiskCalculator createRiskCalculator() {
            System.out.println("[BinanceFactory] Creating RiskCalculator");
            return new RiskCalculator();
        }
    }

    static class YahooIndicatorFactory extends IndicatorFactory {
        @Override
        public Indicator createSMA(int period) {
            System.out.println("[YahooFactory] Creating SMA(" + period + ")");
            return new SMA(period);
        }
        @Override
        public Indicator createATR(int period) {
            System.out.println("[YahooFactory] Creating ATR(" + period + ")");
            return new ATR(period);
        }
        @Override
        public RiskCalculator createRiskCalculator() {
            System.out.println("[YahooFactory] Creating RiskCalculator");
            return new RiskCalculator();
        }
    }
}
