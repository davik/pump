package info.kalyan.krishi.repository;

import info.kalyan.krishi.pojos.Product;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Product findByName(String name);
}
