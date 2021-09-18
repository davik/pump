package info.kalyan.krishi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import info.kalyan.krishi.pojos.Counter;

@Repository
public interface CounterRepository extends MongoRepository<Counter, String> {
}