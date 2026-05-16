import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// ===== Observer Pattern =====
// Publisher (Subject) that fans out PriceData updates to all subscribed observers.
public class DataPublisher {
    public interface Observer {
        void onPriceUpdate(PriceData data);
    }
    private List<Observer> observers = new CopyOnWriteArrayList<>();
    public void subscribe(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }
    public void publish(PriceData data) {
        for (Observer o : observers) {
            o.onPriceUpdate(data);
        }
    }
    public int getObserverCount() {
        return observers.size();
    }
}
