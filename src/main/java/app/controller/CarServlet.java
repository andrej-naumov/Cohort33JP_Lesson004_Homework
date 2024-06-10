package app.controller;

import app.domain.Car;
import app.repository.CarRepository;
import app.repository.CarRepositoryMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CarServlet extends HttpServlet {

    private CarRepository repository = new CarRepositoryMap();

    // GET http://10.2.3.4:8080/cars
    // GET http://10.2.3.4:8080/cars?id=5

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Извлечение параметров запроса
        String maxPriceParam = req.getParameter("maxPrice");
        String sortBy = req.getParameter("sortBy");
        String orderBy = req.getParameter("orderBy");

        // Получение всех автомобилей
        List<Car> cars = repository.getAll();

        // Фильтрация по максимальной цене
        if (maxPriceParam != null) {
            try {
                BigDecimal maxPrice = new BigDecimal(maxPriceParam);
                cars = cars.stream()
                        .filter(car -> car.getPrice().compareTo(maxPrice) <= 0)
                        .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid maxPrice format.");
                return;
            }
        }

        // Сортировка по указанному полю и порядку
        if (sortBy != null) {
            Comparator<Car> comparator = null;
            switch (sortBy) {
                case "brand":
                    comparator = Comparator.comparing(Car::getBrand);
                    break;
                case "price":
                    comparator = Comparator.comparing(Car::getPrice);
                    break;
                case "year":
                    comparator = Comparator.comparing(Car::getYear);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("Invalid sortBy value.");
                    return;
            }

            if (orderBy != null && orderBy.equalsIgnoreCase("desc")) {
                comparator = comparator.reversed();
            }

            cars = cars.stream().sorted(comparator).collect(Collectors.toList());
        }

        // Отправка ответа клиенту
        for (Car car : cars) {
            resp.getWriter().write(car.toString() + "\n");
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Для сохранения нового автомобиля в БД

        ObjectMapper mapper = new ObjectMapper();
        Car car = mapper.readValue(req.getReader(), Car.class);
        repository.save(car);
        resp.getWriter().write("New car saved: " + car.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Чтение JSON из тела запроса
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(req.getReader());

        // Извлечение id и новой цены из JSON
        long id = jsonNode.get("id").asLong();
        BigDecimal newPrice = new BigDecimal(jsonNode.get("price").asText());

        // Поиск автомобиля в базе данных
        Car car = repository.getById(id);

        if (car == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Car with id " + id + " not found.");
            return;
        }

        // Обновление цены автомобиля
        repository.updatePrice(id, newPrice);

        // Отправка успешного ответа
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Car price updated: " + car);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Извлечение параметра id из запроса
        String idParam = req.getParameter("id");

        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameter: id");
            return;
        }

        try {
            long id = Long.parseLong(idParam);

            // Поиск и удаление автомобиля в базе данных
            Car car = repository.getById(id);

            if (car == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Car with id " + id + " not found.");
                return;
            }

            repository.deleteById(id);

            // Отправка успешного ответа
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Car with id " + id + " deleted.");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid id format.");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }
}