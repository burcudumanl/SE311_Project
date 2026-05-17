import java.util.List;


public class RiskCalculator {

    public enum RiskLevel {
        LOW, 
        MEDIUM, 
        HIGH
    }
public double calculateLossPercent(double entryPrice,
                                       double currentPrice) {

        if (entryPrice <= 0) {
            throw new IllegalArgumentException(
                    "Entry price must be greater than zero."
            );
        }

        if (currentPrice < 0) {
            throw new IllegalArgumentException(
                    "Current price cannot be negative."
            );
        }

        double lossPercent =
                ((entryPrice - currentPrice) / entryPrice) * 100;


        if (lossPercent < 0) {
            return 0;
        }

        return lossPercent;
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

      public boolean shouldForceClose(double entryPrice,
                                    double currentPrice,
                                    double maxLossThreshold) {

        return calculateRiskLevel(
                entryPrice,
                currentPrice,
                maxLossThreshold
        ) == RiskLevel.HIGH;
    }

  
}
