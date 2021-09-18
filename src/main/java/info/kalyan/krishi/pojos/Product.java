package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
	@Id
	public String id;
	public String name = "";
	public ProductDTO.Unit unit = ProductDTO.Unit.Litre;
	public double price = 0;
}