// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

// Singleton Pattern
// One shared configuration object holds all runtime settings: data source,
// strategy mode, file paths, risk limits, SMA period, entry price and trade
// size. Every layer (Facade, Factory, RiskCalculator caller) reads from the
// same instance, which prevents inconsistent settings across the system.
public class Configuration {

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
}
