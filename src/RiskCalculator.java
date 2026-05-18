// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

// Computes how much an open position has lost and classifies that into a
// risk level. The Facade uses this to decide whether to force-close a
// position with a SELL order.
//
// A risk-scale lets the IndicatorFactory tune sensitivity per data source
// (e.g. crypto feeds use a tighter scale than delayed equity feeds).
public class RiskCalculator {

    public enum RiskLevel { LOW, MEDIUM, HIGH }

    private final double riskScale;

    public RiskCalculator() {
        this(1.0);
    }

    public RiskCalculator(double riskScale) {
        this.riskScale = riskScale;
    }

    // Returns the loss as a positive percentage; 0 if there is no loss.
    public double calculateLossPercent(double entryPrice, double currentPrice) {
        if (entryPrice <= 0) {
            throw new IllegalArgumentException(
                    "Entry price must be greater than zero.");
        }
        if (currentPrice < 0) {
            throw new IllegalArgumentException(
                    "Current price cannot be negative.");
        }

        double lossPercent = ((entryPrice - currentPrice) / entryPrice) * 100;
        return Math.max(lossPercent, 0);
    }

    // Classifies current loss into LOW / MEDIUM / HIGH using the scaled limit.
    public RiskLevel calculateRiskLevel(double entryPrice,
                                        double currentPrice,
                                        double maxLossPercent) {
        if (entryPrice <= 0) {
            return RiskLevel.HIGH;
        }

        double loss            = ((entryPrice - currentPrice) / entryPrice) * 100;
        double effectiveLimit  = maxLossPercent * riskScale;

        if (loss >= effectiveLimit) {
            return RiskLevel.HIGH;
        } else if (loss >= effectiveLimit / 2) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    // True when the position should be closed (risk level reached HIGH).
    public boolean shouldForceClose(double entryPrice,
                                    double currentPrice,
                                    double maxLossPercent) {
        return calculateRiskLevel(entryPrice, currentPrice, maxLossPercent)
                == RiskLevel.HIGH;
    }
}
