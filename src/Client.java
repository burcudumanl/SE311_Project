// BURCU DUMANLI
// ECE KABASAKAL
// BEYZA BARAK
// ENES YAVUZ
// Algorithmic Trading System

// Application entry point.
// The Client only knows about two design patterns:
//   - Configuration (Singleton): for reading and writing runtime settings.
//   - TradingFacade (Facade):    for running the whole trading workflow.
// Everything else (Adapter, Observer, Factory, indicators, risk, trades)
// is hidden behind the Facade.
public class Client {

    public static void main(String[] args) {
        Configuration config = Configuration.getInstance();

        // Change these values to try a different exchange, dataset or mode.
        config.setExchangeName("Yahoo");
        config.setStrategyName("LONG_TERM");
        config.setCsvFilePath("data/BTC-USD.csv");
        config.setSymbol("BTC-USD");
        config.setSmaPeriod(200);
        config.setEntryPrice(110000.00);
        config.setMaxPositionLoss(4.0);
        config.setTradeQuantity(1);

        new TradingFacade().run();
    }
}
