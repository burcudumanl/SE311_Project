// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

import java.util.List;
import java.util.Locale;

// Facade Pattern
// Single entry point that hides the wiring between every other subsystem:
//   - Configuration  (Singleton) for runtime settings
//   - DataSource     (Adapter)   for fetching CSV / JSON price data
//   - DataPublisher  (Observer)  for pushing price updates to the dashboard
//   - IndicatorFactory (Abstract Factory + Factory Method) for SMA / ATR / Risk
// The Client only talks to this class; it never sees indicators, factories,
// observers or risk objects directly.
public class TradingFacade {

    public enum TradeAction { BUY, SELL }

    private static final String DIVIDER =
            "----------------------------------------------------------------";

    private final Configuration    config;
    private final DataPublisher    pricePublisher;
    private final IndicatorFactory factory;
    private List<PriceData>        priceHistory;

    public TradingFacade() {
        this.config         = Configuration.getInstance();
        this.pricePublisher = new DataPublisher();
        this.factory        = IndicatorFactory.getFactory(config.getExchangeName());
    }

    // Orchestrates the full trading workflow described in the spec:
    //   1) Fetch Data  2) Analyze Indicators  3) Calculate Risk  4) Execute Trade
    // Live dashboard updates are pushed between steps 1 and 2.
    public void run() {
        printBanner("ALGORITHMIC TRADING SYSTEM");
        printConfig();
        loadData();
        runDashboard();
        runTradingCycle();
        printBanner("TRADING SESSION FINISHED");
    }

    private void printBanner(String title) {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("            " + title);
        System.out.println(DIVIDER);
        System.out.println();
    }

    private void printConfig() {
        System.out.println("[CONFIG]");
        System.out.println("  Exchange     : " + config.getExchangeName());
        System.out.println("  Mode         : " + config.getStrategyName());
        System.out.println("  Symbol       : " + config.getSymbol());
        System.out.println("  Data file    : " + config.getCsvFilePath());
        System.out.println("  SMA period   : " + config.getSmaPeriod());
        System.out.printf(Locale.US, "  Entry price  : $%,.2f%n", config.getEntryPrice());
        System.out.printf(Locale.US, "  Max loss     : %.2f%%%n", config.getMaxPositionLoss());
        System.out.println();
    }

    // Step 1: Fetch data via the Adapter.
    private void loadData() {
        DataSource source = new DataSource.Tabular(
                config.getCsvFilePath(),
                config.getSymbol()
        );
        priceHistory = source.fetch();

        System.out.println("[STEP 1] FETCH DATA");
        System.out.println("  Source       : " + source.getName());
        System.out.println("  Rows loaded  : " + priceHistory.size());
        System.out.println();
    }

    // Subscribe dashboard observers and push the last few ticks through them
    // so Chart + P/L produce one aligned row per price update.
    private void runDashboard() {
        if (priceHistory == null || priceHistory.isEmpty()) return;

        pricePublisher.subscribe(new ChartDashboard());
        pricePublisher.subscribe(new ProfitLossCalculator(config.getEntryPrice()));

        System.out.println("[STEP 2] LIVE DASHBOARD (last 3 ticks)");
        System.out.println();
        System.out.println("    Symbol      Close ($)      P/L (%)");
        System.out.println("    --------    ----------    --------");

        int start = Math.max(0, priceHistory.size() - 3);
        for (int i = start; i < priceHistory.size(); i++) {
            pricePublisher.publish(priceHistory.get(i));
        }
        System.out.println();
    }

    // Steps 3, 4 and 5: indicator analysis, risk evaluation, trade execution.
    private void runTradingCycle() {
        if (priceHistory == null || priceHistory.isEmpty()) {
            System.out.println("[STEP 3] No price data available, skipping trade.");
            return;
        }

        PriceData current   = priceHistory.get(priceHistory.size() - 1);
        double    closePrice = current.getClose();

        // Step 3: Analyze indicators.
        Indicator sma = factory.createSMA(config.getSmaPeriod());
        double    smaValue = sma.calculate(priceHistory);
        TradeAction action = closePrice > smaValue ? TradeAction.BUY : TradeAction.SELL;
        String signalNote  = closePrice > smaValue
                ? "price is above SMA"
                : "price is below SMA";

        System.out.println("[STEP 3] ANALYZE INDICATORS");
        System.out.printf(Locale.US, "  Indicator    : %s = %,.2f%n",
                sma.getName(), smaValue);
        System.out.printf(Locale.US, "  Current      : %,.2f%n", closePrice);
        System.out.println("  Signal       : " + action + "   (" + signalNote + ")");
        System.out.println();

        // Step 4: Calculate risk and force-close on breach.
        RiskCalculator risk = factory.createRiskCalculator();
        double lossPercent  = risk.calculateLossPercent(
                config.getEntryPrice(), closePrice);
        RiskCalculator.RiskLevel level = risk.calculateRiskLevel(
                config.getEntryPrice(), closePrice, config.getMaxPositionLoss());
        boolean forceClose = risk.shouldForceClose(
                config.getEntryPrice(), closePrice, config.getMaxPositionLoss());

        System.out.println("[STEP 4] CALCULATE RISK");
        System.out.printf(Locale.US, "  Loss         : %.2f%%%n", lossPercent);
        System.out.println("  Risk level   : " + level);
        if (forceClose) {
            System.out.println("  Force-close  : YES   -> overriding signal to SELL");
            action = TradeAction.SELL;
        } else {
            System.out.println("  Force-close  : NO");
        }
        System.out.println();

        // Step 5: Execute trade.
        System.out.println("[STEP 5] EXECUTE TRADE");
        System.out.printf(Locale.US, "  Order        : %s %d %s @ $%,.2f%n",
                action, config.getTradeQuantity(),
                current.getSymbol(), closePrice);
        System.out.println("  Status       : EXECUTED");
    }
}

// Live dashboard widget #1 (required by spec).
// Prints the symbol and close price portion of a dashboard row. Leaves the
// line open so ProfitLossCalculator can append the P/L column on the same row.
class ChartDashboard implements DataPublisher.Observer {
    @Override
    public void onPriceUpdate(PriceData data) {
        System.out.printf(Locale.US, "    %-8s    %,10.2f    ",
                data.getSymbol(), data.getClose());
    }
}

// Live dashboard widget #2 (required by spec).
// Computes running profit/loss against the configured entry price and closes
// the dashboard row started by ChartDashboard.
class ProfitLossCalculator implements DataPublisher.Observer {
    private final double entryPrice;

    public ProfitLossCalculator(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    @Override
    public void onPriceUpdate(PriceData data) {
        double pnlPercent = ((data.getClose() - entryPrice) / entryPrice) * 100;
        System.out.printf(Locale.US, "%8.2f%n", pnlPercent);
    }
}
