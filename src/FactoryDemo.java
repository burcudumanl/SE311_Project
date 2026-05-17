import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Demonstrates the Abstract Factory + Factory Method patterns via IndicatorFactory.
// Shows that the same client code works identically regardless of which data source
// (Binance or Yahoo) the factory targets — the factory hides all source-specific details.
public class FactoryDemo {
    public static void main(String[] args) {

        // Build a shared set of BTC-USD price bars used by both factories below.
        // Each PriceData entry holds: symbol, timestamp, open, high, low, close, volume.
        List<PriceData> prices = new ArrayList<>();
        prices.add(new PriceData("BTC-USD", Instant.now(), 180.0, 183.5, 179.2, 182.5, 12500));
        prices.add(new PriceData("BTC-USD", Instant.now(), 183.0, 185.8, 181.4, 184.9,  9800));
        prices.add(new PriceData("BTC-USD", Instant.now(), 185.2, 187.0, 184.1, 186.3, 11200));
        prices.add(new PriceData("BTC-USD", Instant.now(), 186.0, 189.0, 185.0, 188.0, 13000));
        prices.add(new PriceData("BTC-USD", Instant.now(), 188.0, 191.0, 187.0, 190.0, 14000));

        System.out.println("=== BINANCE FACTORY ===");
        // getFactory() selects and returns the BinanceIndicatorFactory at runtime —
        // the caller only knows the abstract IndicatorFactory type.
        IndicatorFactory binance = IndicatorFactory.getFactory("Binance");

        // Factory Method: createSMA delegates object creation to BinanceIndicatorFactory.
        Indicator sma = binance.createSMA(3);
        System.out.println(sma.getName() + " result: " + sma.calculate(prices));

        // Factory Method: createATR delegates object creation to BinanceIndicatorFactory.
        Indicator atr = binance.createATR(3);
        System.out.println(atr.getName() + " result: " + atr.calculate(prices));

        // Factory Method: createRiskCalculator returns a risk tool for Binance context.
        RiskCalculator risk = binance.createRiskCalculator();
        double entry = 188.0, current = 178.0, maxLoss = 5.0;
        // Demonstrates loss %, risk level classification, and forced-close decision.
        System.out.printf("Loss: %.2f%% | Risk: %s | ForceClose: %b%n",
                risk.calculateLossPercent(entry, current),
                risk.calculateRiskLevel(entry, current, maxLoss),
                risk.shouldForceClose(entry, current, maxLoss));

        System.out.println("\n=== YAHOO FACTORY ===");
        // Switching to Yahoo: same interface, different concrete factory — no client code change needed.
        IndicatorFactory yahoo = IndicatorFactory.getFactory("Yahoo");

        // Same Factory Method calls; YahooIndicatorFactory handles instantiation internally.
        Indicator sma2 = yahoo.createSMA(5);
        System.out.println(sma2.getName() + " result: " + sma2.calculate(prices));

        Indicator atr2 = yahoo.createATR(3);
        System.out.println(atr2.getName() + " result: " + atr2.calculate(prices));
    }
}
