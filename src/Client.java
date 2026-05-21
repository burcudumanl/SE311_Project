// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Application entry point. Loads settings from data/config.json and runs
// the TradingFacade once per configuration block in the file.
public class Client {

    public static void main(String[] args) {
        Configuration config = Configuration.getInstance();
        String path = "data/config.json";

        int runs = Math.max(1, config.loadConfigCount(path));
        for (int i = 0; i < runs; i++) {
            config.loadFromJson(path, i);
            new TradingFacade().run();
        }
    }
}

// Singleton Pattern
// Holds all runtime settings in one shared instance. Defaults are baked
// into the fields; loadFromJson() overrides any subset of them from disk.
class Configuration {

    private static final Configuration INSTANCE = new Configuration();

    private String exchangeName     = "Yahoo";
    private String strategyName     = "LONG_TERM";
    private String csvFilePath      = "data/BTC-USD.csv";
    private String symbol           = "BTC-USD";
    private int    smaPeriod        = 200;
    private double entryPrice       = 110000.00;
    private double maxPositionLoss  = 4.0;
    private int    tradeQuantity    = 1;

    private Configuration() { }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getExchangeName()                   { return exchangeName; }
    public void   setExchangeName(String v)           { this.exchangeName = v; }

    public String getStrategyName()                   { return strategyName; }
    public void   setStrategyName(String v)           { this.strategyName = v; }

    public String getCsvFilePath()                    { return csvFilePath; }
    public void   setCsvFilePath(String v)            { this.csvFilePath = v; }

    public String getSymbol()                         { return symbol; }
    public void   setSymbol(String v)                 { this.symbol = v; }

    public int    getSmaPeriod()                      { return smaPeriod; }
    public void   setSmaPeriod(int v)                 { this.smaPeriod = v; }

    public double getEntryPrice()                     { return entryPrice; }
    public void   setEntryPrice(double v)             { this.entryPrice = v; }

    public double getMaxPositionLoss()                { return maxPositionLoss; }
    public void   setMaxPositionLoss(double v)        { this.maxPositionLoss = v; }

    public int    getTradeQuantity()                  { return tradeQuantity; }
    public void   setTradeQuantity(int v)             { this.tradeQuantity = v; }

    // Number of { ... } blocks in the JSON file. 0 if file is missing.
    public int loadConfigCount(String path) {
        return readConfigBlocks(path).size();
    }

    // Applies the N-th block to this Singleton; missing fields are untouched.
    public void loadFromJson(String path, int index) {
        List<String> blocks = readConfigBlocks(path);
        if (blocks.isEmpty()) {
            System.out.println("[CONFIG] " + path
                    + " not found, using built-in defaults.");
            return;
        }
        if (index < 0 || index >= blocks.size()) {
            return;
        }
        applyBlock(blocks.get(index));
        System.out.println("[CONFIG] Loaded block #" + (index + 1)
                + " from " + path);
    }

    public void loadFromJson(String path) {
        loadFromJson(path, 0);
    }

    private void applyBlock(String json) {
        String s;
        if ((s = readString(json, "exchangeName")) != null) this.exchangeName = s;
        if ((s = readString(json, "strategyName")) != null) this.strategyName = s;
        if ((s = readString(json, "csvFilePath"))  != null) this.csvFilePath  = s;
        if ((s = readString(json, "symbol"))       != null) this.symbol       = s;

        Integer i;
        if ((i = readInt(json, "smaPeriod"))     != null) this.smaPeriod     = i;
        if ((i = readInt(json, "tradeQuantity")) != null) this.tradeQuantity = i;

        Double d;
        if ((d = readDouble(json, "entryPrice"))      != null) this.entryPrice      = d;
        if ((d = readDouble(json, "maxPositionLoss")) != null) this.maxPositionLoss = d;
    }

    // Extracts every top-level { ... } object as a separate string.
    private static List<String> readConfigBlocks(String path) {
        List<String> result = new java.util.ArrayList<>();
        String json;
        try {
            json = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            return result;
        }
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) current.setLength(0);
                depth++;
                current.append(c);
            } else if (c == '}') {
                current.append(c);
                depth--;
                if (depth == 0) result.add(current.toString());
            } else if (depth > 0) {
                current.append(c);
            }
        }
        return result;
    }

    private static String readString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static Integer readInt(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static Double readDouble(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : null;
    }
}

// Facade Pattern
// Single entry point that wires Singleton, Adapter, Observer and the
// Abstract Factory together so the Client never sees them directly.
class TradingFacade {

    enum TradeAction { BUY, SELL }

    private static final String DIVIDER =
            "----------------------------------------------------------------";

    private final Configuration    config;
    private final DataPublisher    pricePublisher;
    private final IndicatorFactory factory;
    private List<PriceData>        priceHistory;

    TradingFacade() {
        this.config         = Configuration.getInstance();
        this.pricePublisher = new DataPublisher();
        this.factory        = IndicatorFactory.getFactory(config.getExchangeName());
    }

    // Fetch -> Dashboard -> Analyze -> Risk -> Execute.
    void run() {
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

    // Step 2: Subscribe dashboard observers and publish the last few ticks.
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

    // Steps 3-5: indicator analysis, risk evaluation, trade execution.
    private void runTradingCycle() {
        if (priceHistory == null || priceHistory.isEmpty()) {
            System.out.println("[STEP 3] No price data available, skipping trade.");
            return;
        }

        PriceData current    = priceHistory.get(priceHistory.size() - 1);
        double    closePrice = current.getClose();

        // Step 3: Analyze indicators.
        Indicator sma      = factory.createSMA(config.getSmaPeriod());
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

        // Step 4: Calculate risk and decide whether to force-close.
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
        } else {
            System.out.println("  Force-close  : NO");
        }
        System.out.println();

        // Step 5: Execute one trade per bar for the last 3 bars.
        System.out.println("[STEP 5] EXECUTE TRADES (last 3 bars)");
        System.out.println();
        System.out.println("    Close ($)      Signal    Loss(%)    Risk      Final Order");
        System.out.println("    ----------    ------    -------    ------    -----------------------");

        int start = Math.max(0, priceHistory.size() - 3);
        for (int i = start; i < priceHistory.size(); i++) {
            PriceData bar  = priceHistory.get(i);
            double barClose = bar.getClose();

            TradeAction barAction = barClose > smaValue
                    ? TradeAction.BUY : TradeAction.SELL;
            double barLoss = risk.calculateLossPercent(
                    config.getEntryPrice(), barClose);
            RiskCalculator.RiskLevel barLevel = risk.calculateRiskLevel(
                    config.getEntryPrice(), barClose, config.getMaxPositionLoss());
            if (risk.shouldForceClose(
                    config.getEntryPrice(), barClose, config.getMaxPositionLoss())) {
                barAction = TradeAction.SELL;
            }

            System.out.printf(Locale.US,
                    "    %,10.2f    %-6s    %7.2f    %-6s    %s %d %s @ $%,.2f%n",
                    barClose, barAction, barLoss, barLevel,
                    barAction, config.getTradeQuantity(),
                    bar.getSymbol(), barClose);
        }
        System.out.println();
        System.out.println("  Status       : EXECUTED");
    }
}

// Observer #1: prints the symbol and close price; leaves the row open so
// ProfitLossCalculator can append the P/L column on the same line.
class ChartDashboard implements DataPublisher.Observer {
    @Override
    public void onPriceUpdate(PriceData data) {
        System.out.printf(Locale.US, "    %-8s    %,10.2f    ",
                data.getSymbol(), data.getClose());
    }
}

// Observer #2: computes P/L vs. entry price and closes the dashboard row.
class ProfitLossCalculator implements DataPublisher.Observer {
    private final double entryPrice;

    ProfitLossCalculator(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    @Override
    public void onPriceUpdate(PriceData data) {
        double pnlPercent = ((data.getClose() - entryPrice) / entryPrice) * 100;
        System.out.printf(Locale.US, "%8.2f%n", pnlPercent);
    }
}
