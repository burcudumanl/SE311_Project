// Abstract Factory: defines a contract for creating a family of related objects
// (SMA indicator, ATR indicator, RiskCalculator) without specifying their concrete classes.
// Each concrete subclass represents one data-source "variant" of the factory.
// Factory Method pattern is applied inside each abstract method (createSMA, createATR,
// createRiskCalculator): subclasses decide which concrete object to instantiate,
// keeping object creation decoupled from the code that uses the objects.
public abstract class IndicatorFactory {

    // Factory Method: subclasses override this to produce a source-specific SMA indicator.
    public abstract Indicator createSMA(int period);

    // Factory Method: subclasses override this to produce a source-specific ATR indicator.
    public abstract Indicator createATR(int period);

    // Factory Method: subclasses override this to produce a source-specific RiskCalculator.
    public abstract RiskCalculator createRiskCalculator();

    // Static factory selector (Factory Method at the top level):
    // maps a data-source name to the correct concrete factory, so callers never
    // need to know which factory class to instantiate directly.
    public static IndicatorFactory getFactory(String source) {
        switch (source) {
            case "Binance": return new BinanceIndicatorFactory();
            case "Yahoo":   return new YahooIndicatorFactory();
            default: throw new IllegalArgumentException("Unknown source: " + source);
        }
    }

    // Concrete factory for Binance data: creates indicators and risk tools
    // tagged and configured for the Binance data source.
    static class BinanceIndicatorFactory extends IndicatorFactory {
        @Override
        // Creates an SMA indicator sourced from Binance price data.
        public Indicator createSMA(int period) {
            System.out.println("[BinanceFactory] Creating SMA(" + period + ")");
            return new SMA(period);
        }
        @Override
        // Creates an ATR indicator sourced from Binance price data.
        public Indicator createATR(int period) {
            System.out.println("[BinanceFactory] Creating ATR(" + period + ")");
            return new ATR(period);
        }
        @Override
        // Creates a RiskCalculator configured for Binance workflows.
        public RiskCalculator createRiskCalculator() {
            System.out.println("[BinanceFactory] Creating RiskCalculator");
            return new RiskCalculator();
        }
    }

    // Concrete factory for Yahoo Finance data: same product family as Binance
    // but tagged for Yahoo, allowing future source-specific customization.
    static class YahooIndicatorFactory extends IndicatorFactory {
        @Override
        // Creates an SMA indicator sourced from Yahoo Finance price data.
        public Indicator createSMA(int period) {
            System.out.println("[YahooFactory] Creating SMA(" + period + ")");
            return new SMA(period);
        }
        @Override
        // Creates an ATR indicator sourced from Yahoo Finance price data.
        public Indicator createATR(int period) {
            System.out.println("[YahooFactory] Creating ATR(" + period + ")");
            return new ATR(period);
        }
        @Override
        // Creates a RiskCalculator configured for Yahoo Finance workflows.
        public RiskCalculator createRiskCalculator() {
            System.out.println("[YahooFactory] Creating RiskCalculator");
            return new RiskCalculator();
        }
    }
}
