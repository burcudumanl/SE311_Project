public class ConsoleObserver implements DataObserver {

    private String name;

    public ConsoleObserver(String name) {
        this.name = name;
    }

    @Override
    public void onPriceUpdate(PriceData data) {
        System.out.println("[" + name + "] received: " + data);
    }
}
