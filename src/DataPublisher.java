import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataPublisher {

    private List<DataObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(DataObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(DataObserver observer) {
        observers.remove(observer);
    }

    public void publish(PriceData data) {
        for (DataObserver o : observers) {
            o.onPriceUpdate(data);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}
