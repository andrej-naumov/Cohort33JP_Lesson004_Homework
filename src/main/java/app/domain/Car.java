package app.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Car {

    private Long id;
    private String brand;
    private BigDecimal price;
    private int year;

    public Car(String brand, BigDecimal price, int year) {
        this.brand = brand;
        this.price = price;
        this.year = year;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return year == car.year && Objects.equals(id, car.id) && Objects.equals(brand, car.brand) && Objects.equals(price, car.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brand, price, year);
    }

    @Override
    public String toString() {
        return String.format("Car: id - %d, brand - %s, price - %s, year - %d",
                id, brand, price, year);
    }
}