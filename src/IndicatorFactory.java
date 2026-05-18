// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

// Abstract Factory Pattern:
//   Declares the contract for creating a family of related products
//   (SMA indicator, ATR indicator, RiskCalculator) without binding the
//   caller to any concrete data-source class.
//
// Factory Method Pattern:
//   Each create* method is a factory method that subclasses override to
//   decide which concrete object to instantiate. The static getFactory()
//   below is also a factory method, choosing which concrete factory to
//   return based on a runtime source name.
public abstract class IndicatorFactory {

    public abstract Indicator      createSMA(int period);
    public abstract Indicator      createATR(int period);
    public abstract RiskCalculator createRiskCalculator();

    // Returns the concrete factory matching the configured data source.
    public static IndicatorFactory getFactory(String source) {
        switch (source) {
            case "Binance": return new BinanceIndicatorFactory();
            case "Yahoo":   return new YahooIndicatorFactory();
            default:
                throw new IllegalArgumentException("Unknown source: " + source);
        }
    }

    // Concrete factory for Binance feeds. Indicators are tagged "Binance"
    // and the RiskCalculator uses a tighter multiplier because crypto feeds
    // are typically more volatile than equity feeds.
    static class BinanceIndicatorFactory extends IndicatorFactory {

        private static final String  TAG        = "Binance";
        private static final double  RISK_SCALE = 0.8;

        @Override
        public Indicator createSMA(int period) {
            return new SMA(period, TAG);
        }
        @Override
        public Indicator createATR(int period) {
            return new ATR(period, TAG);
        }
        @Override
        public RiskCalculator createRiskCalculator() {
            return new RiskCalculator(RISK_SCALE);
        }
    }

    // Concrete factory for Yahoo Finance feeds. Indicators are tagged
    // "Yahoo" and the RiskCalculator uses the default (more conservative)
    // multiplier appropriate for delayed equity data.
    static class YahooIndicatorFactory extends IndicatorFactory {

        private static final String TAG = "Yahoo";

        @Override
        public Indicator createSMA(int period) {
            return new SMA(period, TAG);
        }
        @Override
        public Indicator createATR(int period) {
            return new ATR(period, TAG);
        }
        @Override
        public RiskCalculator createRiskCalculator() {
            return new RiskCalculator();
        }
    }
}
