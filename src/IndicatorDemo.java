import java.util.List;

public class IndicatorDemo {

    public static void main(String[] args) {

        System.out.println("Indicator and Risk Demo");

        DataSource source = new DataSource.Json("data/sample_feed.json");
        List<PriceData> prices = source.fetch();

        if (prices.isEmpty()) {
            System.out.println("No data has been found, check the data file path.");
            return;
        }

        System.out.println("Loaded records: " + prices.size());

        int period = 3;

        Indicator sma = new SMA(period);
        Indicator atr = new ATR(period);

        double smaValue = sma.calculate(prices);
        double atrValue = atr.calculate(prices);

        PriceData lastRecord = prices.get(prices.size() - 1);
        double currentPrice = lastRecord.getClose();

        System.out.println();
        System.out.println(sma.getName() + " value is: " + smaValue);
        System.out.println(atr.getName() + " value is: " + atrValue);
        System.out.println("Current close price is: " + currentPrice);

        System.out.println();

        if (currentPrice > smaValue) {
            System.out.println("SMA check: current price is above SMA.");
            System.out.println("Possible order decision: BUY");
        } else {
            System.out.println("SMA check: current price is not above SMA.");
            System.out.println("Possible order decision: SELL");
        }

        System.out.println();

        RiskCalculator riskCalculator = new RiskCalculator();

        double entryPrice = 180.00;
        double maxLossThreshold = 4.0;

        double lossPercent =
                riskCalculator.calculateLossPercent(entryPrice, currentPrice);

        RiskCalculator.RiskLevel riskLevel =
                riskCalculator.calculateRiskLevel(entryPrice, currentPrice, maxLossThreshold);

        System.out.println("Entry price is: " + entryPrice);
        System.out.println("Loss percent is: " + lossPercent + "%");
        System.out.println("Maximum allowed loss is: " + maxLossThreshold + "%");
        System.out.println("Risk level is: " + riskLevel);

        if (riskCalculator.shouldForceClose(entryPrice, currentPrice, maxLossThreshold)) {
            System.out.println("Risk action: position should be closed with a SELL order.");
        } else {
            System.out.println("Risk action: position can stay open.");
        }

        System.out.println();
        System.out.println("Demo is over.");
    }
}
