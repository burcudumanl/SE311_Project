// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

import java.util.List;

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

    private final Configuration     config;
    private final DataPublisher     pricePublisher;
    private final IndicatorFactory  factory;
    private List<PriceData>         priceHistory;

    public TradingFacade() {
        this.config         = Configuration.getInstance();
        this.pricePublisher = new DataPublisher();
        this.factory        = IndicatorFactory.getFactory(config.getExchangeName());
    }

    // Orchestrates the full trading workflow described in the spec:
    //   1) Fetch Data  2) Analyze Indicators  3) Calculate Risk  4) Execute Trade
    public void run() {
        System.out.println("=== TRADING SESSION STARTED ===");
        setupDashboard();
        loadData();
        runTradingCycle();
        System.out.println("=== TRADING SESSION FINISHED ===");
    }

    // Registers the two live dashboard widgets required by the spec.
    private void setupDashboard() {
        pricePublisher.subscribe(new ChartDashboard());
        pricePublisher.subscribe(new ProfitLossCalculator(config.getEntryPrice()));
        System.out.println("[Facade] Dashboard ready (Chart + P/L).");
    }

    // Step 1: Fetch data via the Adapter and push the latest rows to dashboards.
    private void loadData() {
        DataSource source = new DataSource.Tabular(
                config.getCsvFilePath(),
                config.getSymbol()
        );
        priceHistory = source.fetch();

        System.out.println("[Facade] Loaded " + priceHistory.size()
                + " rows from " + source.getName());

        // Send the last few rows to the live dashboard so observers fire.
        int start = Math.max(0, priceHistory.size() - 3);
        for (int i = start; i < priceHistory.size(); i++) {
            pricePublisher.publish(priceHistory.get(i));
        }
    }

    // Steps 2, 3 and 4 of the workflow.
    private void runTradingCycle() {
        if (priceHistory == null || priceHistory.isEmpty()) {
            System.out.println("[Facade] No price data available, skipping cycle.");
            return;
        }

        PriceData current = priceHistory.get(priceHistory.size() - 1);

        // Step 2: Analyze indicators.
        TradeAction action = decideAction(current);

        // Step 3: Calculate risk and force-close on breach.
        RiskCalculator risk = factory.createRiskCalculator();
        RiskCalculator.RiskLevel level = risk.calculateRiskLevel(
                config.getEntryPrice(),
                current.getClose(),
                config.getMaxPositionLoss()
        );
        System.out.println("[Facade] Risk level: " + level);

        if (risk.shouldForceClose(
                config.getEntryPrice(),
                current.getClose(),
                config.getMaxPositionLoss())) {
            System.out.println("[Facade] Risk limit breached -> forcing SELL.");
            action = TradeAction.SELL;
        }

        // Step 4: Execute trade.
        executeTrade(current.getSymbol(), action, current.getClose());
    }

    // Single decision rule shared by short-term and long-term modes:
    //   - If current price is above the SMA  -> BUY
    //   - Otherwise                          -> SELL
    // The difference between SHORT_TERM and LONG_TERM is only the data
    // resolution and the SMA period, both held by the Configuration.
    private TradeAction decideAction(PriceData current) {
        Indicator sma = factory.createSMA(config.getSmaPeriod());
        double smaValue   = sma.calculate(priceHistory);
        double closePrice = current.getClose();

        System.out.println("[Facade] Mode: " + config.getStrategyName());
        System.out.println("[Facade] " + sma.getName() + " = " + smaValue);
        System.out.println("[Facade] Current close = " + closePrice);

        return closePrice > smaValue ? TradeAction.BUY : TradeAction.SELL;
    }

    // Step 4 of the workflow: prints a simple "executed" log line.
    private void executeTrade(String symbol, TradeAction action, double price) {
        System.out.println("[Facade] EXECUTED "
                + action + " " + config.getTradeQuantity()
                + " " + symbol + " @ " + price);
    }
}

// Live dashboard widget #1 (required by spec).
// Receives every new PriceData update and prints the latest close.
class ChartDashboard implements DataPublisher.Observer {
    @Override
    public void onPriceUpdate(PriceData data) {
        System.out.println("[Chart] " + data.getSymbol()
                + " close=" + data.getClose());
    }
}

// Live dashboard widget #2 (required by spec).
// Tracks running profit/loss against the entry price configured at startup.
class ProfitLossCalculator implements DataPublisher.Observer {
    private final double entryPrice;

    public ProfitLossCalculator(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    @Override
    public void onPriceUpdate(PriceData data) {
        double pnlPercent = ((data.getClose() - entryPrice) / entryPrice) * 100;
        System.out.printf("[P/L]   %s pnl=%.2f%%%n", data.getSymbol(), pnlPercent);
    }
}
