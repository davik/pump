package info.kalyan.krishi.pojos;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 10L;
	@Id
	public String id;
	public String username;
	public String password;
	public String fullname;
	public boolean enabled = false;
}
