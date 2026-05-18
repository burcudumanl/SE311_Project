import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        SystemConfig config = SystemConfig.getInstance();

        // Burayi degistirerek sistemi farkli data/strategy ile deneyebiliriz.
        config.setExchangeName("Yahoo");
        config.setStrategyName("LONG_TERM");
        config.setCsvFilePath("data/BTC-USD.csv");
        config.setSymbol("BTC-USD");
        config.setSmaPeriod(200);
        config.setEntryPrice(110000.00);
        config.setMaxPositionLoss(4.0);
        config.setTradeQuantity(1);

        TradingSystemFacade tradingSystem = new TradingSystemFacade();
        LocalTradingServer server = new LocalTradingServer(tradingSystem);

        server.run(config.getCsvFilePath(), config.getSymbol());
    }
}

// Facade Pattern
// Client sadece bu class ile konusuyor. Data okuma, indicator, risk ve trade
// islemlerini tek tek bilmesine gerek kalmiyor.
class TradingSystemFacade {
    private SystemConfig config;
    private DataPublisher pricePublisher;
    private SystemEventPublisher eventPublisher;
    private IndicatorFactory factory;
    private TradeExecutor tradeExecutor;
    private List<PriceData> priceHistory;

    public TradingSystemFacade() {
        config = SystemConfig.getInstance();
        pricePublisher = new DataPublisher();
        eventPublisher = new SystemEventPublisher();
        factory = IndicatorFactory.getFactory(config.getExchangeName());
        tradeExecutor = new TradeExecutor();
    }

    public void setupDashboard() {
        pricePublisher.subscribe(new ChartDashboard());
        pricePublisher.subscribe(new ProfitLossCalculator(config.getEntryPrice()));
        eventPublisher.subscribe(new DashboardAlert());
        System.out.println("[Facade] Dashboard observers are ready.");
    }

    public void loadCsvData(String filePath, String symbol) {
        DataSource source = new DataSource.Tabular(filePath, symbol);
        priceHistory = source.fetch();

        System.out.println("[Facade] CSV loaded from " + filePath);
        System.out.println("[Facade] Row count: " + priceHistory.size());

        // Tum dosyayi bastirmayalim, son 3 update dashboard'a gitsin yeter.
        int start = Math.max(0, priceHistory.size() - 3);
        for (int i = start; i < priceHistory.size(); i++) {
            pricePublisher.publish(priceHistory.get(i));
        }
    }

    public void runTradingCycle() {
        if (priceHistory == null || priceHistory.isEmpty()) {
            System.out.println("[Facade] No price data, so trading cycle stopped.");
            return;
        }

        TradingStrategy strategy = createStrategy();
        PriceData currentData = priceHistory.get(priceHistory.size() - 1);
        TradeAction action = strategy.decide(priceHistory, factory, config);

        RiskCalculator riskCalculator = factory.createRiskCalculator();
        RiskCalculator.RiskLevel riskLevel = riskCalculator.calculateRiskLevel(
                config.getEntryPrice(),
                currentData.getClose(),
                config.getMaxPositionLoss()
        );

        System.out.println("[Facade] Risk level: " + riskLevel);
        eventPublisher.publish("Risk level calculated as " + riskLevel);

        // Risk cok yuksekse strategy ne derse desin pozisyonu kapatiyoruz.
        if (riskCalculator.shouldForceClose(
                config.getEntryPrice(),
                currentData.getClose(),
                config.getMaxPositionLoss())) {
            System.out.println("[Facade] Risk limit passed. Trade action changed to SELL.");
            eventPublisher.publish("Risk limit passed, position should be closed.");
            action = TradeAction.SELL;
        }

        TradeOrder order = tradeExecutor.execute(
                currentData.getSymbol(),
                action,
                currentData.getClose(),
                config.getTradeQuantity()
        );

        System.out.println("[Facade] Final order: " + order);
        eventPublisher.publish("Trade executed: " + order);
    }

    private TradingStrategy createStrategy() {
        if ("SHORT_TERM".equalsIgnoreCase(config.getStrategyName())) {
            return new ShortTermStrategy();
        }
        return new LongTermStrategy();
    }
}

// Server gibi dusunebiliriz. Simdilik gercek socket yok, sadece client istegini
// alip facade'a dogru sirayla is yaptiriyor.
class LocalTradingServer {
    private TradingSystemFacade facade;

    public LocalTradingServer(TradingSystemFacade facade) {
        this.facade = facade;
    }

    public void run(String filePath, String symbol) {
        System.out.println("=== CLIENT REQUEST STARTED ===");
        facade.setupDashboard();
        facade.loadCsvData(filePath, symbol);
        facade.runTradingCycle();
        System.out.println("=== CLIENT REQUEST FINISHED ===");
    }
}

// Singleton Pattern
// Projede tek ortak ayar objesi olsun diye bunu kullandik.
class SystemConfig {
    private static SystemConfig instance = new SystemConfig();

    private String exchangeName = "Yahoo";
    private String strategyName = "LONG_TERM";
    private String csvFilePath = "data/BTC-USD.csv";
    private String symbol = "BTC-USD";
    private int smaPeriod = 200;
    private double entryPrice = 110000.00;
    private double maxPositionLoss = 4.0;
    private int tradeQuantity = 1;

    private SystemConfig() {
    }

    public static SystemConfig getInstance() {
        return instance;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getSmaPeriod() {
        return smaPeriod;
    }

    public void setSmaPeriod(int smaPeriod) {
        this.smaPeriod = smaPeriod;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getMaxPositionLoss() {
        return maxPositionLoss;
    }

    public void setMaxPositionLoss(double maxPositionLoss) {
        this.maxPositionLoss = maxPositionLoss;
    }

    public int getTradeQuantity() {
        return tradeQuantity;
    }

    public void setTradeQuantity(int tradeQuantity) {
        this.tradeQuantity = tradeQuantity;
    }
}

interface TradingStrategy {
    TradeAction decide(List<PriceData> prices, IndicatorFactory factory, SystemConfig config);
}

class ShortTermStrategy implements TradingStrategy {
    @Override
    public TradeAction decide(List<PriceData> prices, IndicatorFactory factory, SystemConfig config) {
        System.out.println("[Strategy] Short term strategy selected.");
        return decideWithSma(prices, factory, config);
    }

    private TradeAction decideWithSma(List<PriceData> prices, IndicatorFactory factory, SystemConfig config) {
        Indicator sma = factory.createSMA(config.getSmaPeriod());
        double smaValue = sma.calculate(prices);
        double currentPrice = prices.get(prices.size() - 1).getClose();

        System.out.println("[Strategy] Current price: " + currentPrice);
        System.out.println("[Strategy] " + sma.getName() + ": " + smaValue);

        if (currentPrice > smaValue) {
            return TradeAction.BUY;
        }
        return TradeAction.SELL;
    }
}

class LongTermStrategy implements TradingStrategy {
    @Override
    public TradeAction decide(List<PriceData> prices, IndicatorFactory factory, SystemConfig config) {
        System.out.println("[Strategy] Long term strategy selected.");
        return decideWithSma(prices, factory, config);
    }

    private TradeAction decideWithSma(List<PriceData> prices, IndicatorFactory factory, SystemConfig config) {
        Indicator sma = factory.createSMA(config.getSmaPeriod());
        double smaValue = sma.calculate(prices);
        double currentPrice = prices.get(prices.size() - 1).getClose();

        System.out.println("[Strategy] Current price: " + currentPrice);
        System.out.println("[Strategy] " + sma.getName() + ": " + smaValue);

        if (currentPrice > smaValue) {
            return TradeAction.BUY;
        }
        return TradeAction.SELL;
    }
}

enum TradeAction {
    BUY,
    SELL
}

class TradeExecutor {
    public TradeOrder execute(String symbol, TradeAction action, double price, int quantity) {
        System.out.println("[TradeExecutor] Executing " + action + " order.");
        return new TradeOrder(symbol, action, price, quantity, "EXECUTED");
    }
}

class TradeOrder {
    private String symbol;
    private TradeAction action;
    private double price;
    private int quantity;
    private String status;

    public TradeOrder(String symbol, TradeAction action, double price, int quantity, String status) {
        this.symbol = symbol;
        this.action = action;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
    }

    @Override
    public String toString() {
        return action + " " + quantity + " " + symbol + " at " + price + " (" + status + ")";
    }
}

// Bu da Observer'in trade/risk mesaji versiyonu gibi.
class SystemEventPublisher {
    public interface Observer {
        void onSystemEvent(String message);
    }

    private List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void publish(String message) {
        for (Observer observer : observers) {
            observer.onSystemEvent(message);
        }
    }
}

class ChartDashboard implements DataPublisher.Observer {
    @Override
    public void onPriceUpdate(PriceData data) {
        System.out.println("[Chart] " + data.getSymbol() + " close price: " + data.getClose());
    }
}

class DashboardAlert implements SystemEventPublisher.Observer {
    @Override
    public void onSystemEvent(String message) {
        System.out.println("[Dashboard Alert] " + message);
    }
}

class ProfitLossCalculator implements DataPublisher.Observer {
    private double entryPrice;

    public ProfitLossCalculator(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    @Override
    public void onPriceUpdate(PriceData data) {
        double profitLoss = ((data.getClose() - entryPrice) / entryPrice) * 100;
        System.out.println("[P/L] " + data.getSymbol() + " profit/loss: " + profitLoss + "%");
    }
}
