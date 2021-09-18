package info.kalyan.krishi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import info.kalyan.krishi.pojos.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	User findByUsername(String username);
}