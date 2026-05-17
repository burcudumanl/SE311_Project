import java.util.List;


public class RiskCalculator {

    public enum RiskLevel {
        LOW, 
        MEDIUM, 
        HIGH
    }

    public RiskLevel calculateRiskLevel(double entryPrice, double currentPrice, double maxLossPercent) {
        if (entryPrice <= 0) {
            return RiskLevel.HIGH;
        }

        double loss = ((entryPrice - currentPrice) / entryPrice) * 100;

        if (loss >= maxLossPercent) {
            return RiskLevel.HIGH;
        } else if (loss >= maxLossPercent / 2) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    public double calculateVolatility(List<PriceData> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }

        double totalRange = 0;

        for (PriceData price : data) {
            totalRange += price.getHigh() - price.getLow();
        }

        return totalRange / data.size();
    }

    public double calculatePositionSize(double accountBalance, double riskPercent,
                                        double entryPrice, double stopLossPrice) {
        double moneyAtRisk = accountBalance * (riskPercent / 100);
        double riskPerUnit = Math.abs(entryPrice - stopLossPrice);

        if (riskPerUnit == 0) {
            return 0;
        }

        return moneyAtRisk / riskPerUnit;
    }
}
