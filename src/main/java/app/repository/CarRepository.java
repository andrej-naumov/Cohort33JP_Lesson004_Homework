package app.repository;

import app.domain.Car;

import java.util.List;

public interface CarRepository {

    List<Car> getAll();

    Car save(Car car);
}