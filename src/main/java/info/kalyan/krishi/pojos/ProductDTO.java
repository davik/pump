package info.kalyan.krishi.pojos;

public class ProductDTO {
    public enum Unit {
        Bottle, Litre
    }

    public String name = "";
    public Unit unit = Unit.Litre;
    public double price = 0;
}