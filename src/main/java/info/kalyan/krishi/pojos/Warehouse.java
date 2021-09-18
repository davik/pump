package info.kalyan.krishi.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "warehouses")
public class Warehouse {
	@Id
	public String id;
	public String name = "";
	public String location = "";

	public Warehouse() {
	}

	public Warehouse(String id, String name, String location) {
		this.id = id;
		this.name = name;
		this.location = location;
	}

}