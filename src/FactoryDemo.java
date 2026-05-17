import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FactoryDemo {
    public static void main(String[] args) {

        List<PriceData> prices = new ArrayList<>();
        prices.add(new PriceData("BTC-USD", Instant.now(), 180.0, 183.5, 179.2, 182.5, 12500));
        prices.add(new PriceData("BTC-USD", Instant.now(), 183.0, 185.8, 181.4, 184.9,  9800));
        prices.add(new PriceData("BTC-USD", Instant.now(), 185.2, 187.0, 184.1, 186.3, 11200));
        prices.add(new PriceData("BTC-USD", Instant.now(), 186.0, 189.0, 185.0, 188.0, 13000));
        prices.add(new PriceData("BTC-USD", Instant.now(), 188.0, 191.0, 187.0, 190.0, 14000));

        System.out.println("=== BINANCE FACTORY ===");
        IndicatorFactory binance = IndicatorFactory.getFactory("Binance");

        Indicator sma = binance.createSMA(3);
        System.out.println(sma.getName() + " result: " + sma.calculate(prices));

        Indicator atr = binance.createATR(3);
        System.out.println(atr.getName() + " result: " + atr.calculate(prices));

        RiskCalculator risk = binance.createRiskCalculator();
        double entry = 188.0, current = 178.0, maxLoss = 5.0;
        System.out.printf("Loss: %.2f%% | Risk: %s | ForceClose: %b%n",
                risk.calculateLossPercent(entry, current),
                risk.calculateRiskLevel(entry, current, maxLoss),
                risk.shouldForceClose(entry, current, maxLoss));

        System.out.println("\n=== YAHOO FACTORY ===");
        IndicatorFactory yahoo = IndicatorFactory.getFactory("Yahoo");

        Indicator sma2 = yahoo.createSMA(5);
        System.out.println(sma2.getName() + " result: " + sma2.calculate(prices));

        Indicator atr2 = yahoo.createATR(3);
        System.out.println(atr2.getName() + " result: " + atr2.calculate(prices));
    }
}
