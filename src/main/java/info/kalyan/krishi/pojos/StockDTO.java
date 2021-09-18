package info.kalyan.krishi.pojos;

import java.util.ArrayList;
import java.util.List;

public class StockDTO {
    public String productId;
    public String productName;
    public ProductDTO.Unit unit;
    public double price;

    public List<Integer> currentStocks = new ArrayList<>();
}
