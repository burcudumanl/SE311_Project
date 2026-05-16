import java.util.List;

public interface DataSource {
    List<PriceData> fetch();
    String getName();
}
