package info.kalyan.krishi.repository;

import info.kalyan.krishi.pojos.Warehouse;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {
    Warehouse findByName(String name);
}
