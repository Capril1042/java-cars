package com.cjs.cars;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class CarController
{
    private final CarRepository carRepos;
    private final RabbitTemplate rt;

    public CarController(CarRepository carRepos, RabbitTemplate rt)
    {
        this.carRepos = carRepos;
        this.rt = rt;
    }

    //GET ENDPOINTS

    @GetMapping("/cars/all")
    public List<Car> all()
    {
        return carRepos.findAll();
    }

    @GetMapping("/cars/id/{id}")  //returns the car based of of id
    public Car findOne(@PathVariable Long id)
    {
        return carRepos.findById(id)
                .orElseThrow(() -> new CarNotFoundException(id));
    }

    @GetMapping("/cars/year/{year}") // returns a list of cars of that year model
    public List<Car> findByYear(@PathVariable int year)
    {
        List<Car> cars = carRepos.findAll();
        List<Car> carsByYear = new ArrayList<Car>();
        for (Car c : cars)
        {
            if (c.getYear() == year)
            {
                carsByYear.add(c);
            }
        }
        return carsByYear;

    }

    @GetMapping("cars/brand/{brand}") // returns a list of cars of that brand
// This gets logged with a message of "search for {brand}".
// So put the brand of the car that was searched in the message itself.
    public List<Car> findByBrand(@PathVariable String brand)
    {
        List<Car> cars = carRepos.findAll();
        List<Car> carsByBrand = new ArrayList<Car>();
        for (Car c : cars)
        {
            if (c.getBrand().equalsIgnoreCase(brand))
            {
                carsByBrand.add(c);
            }
        }
        CarLog message = new CarLog(" Search for " + brand);
        rt.convertAndSend(CarsApplication.QUEUE_NAME, message.toString());
        log.info(" search for " + brand);
        return carsByBrand;

    }

    @PostMapping("/cars/upload") // loads multiple sets of data from the RequestBody
    // This gets logged with a message of "Data loaded"
    public List<Car> newCar(@RequestBody List<Car> newCars)
    {
        CarLog message = new CarLog("Data Loaded");
        rt.convertAndSend(CarsApplication.QUEUE_NAME, message.toString());
        log.info("Data Loaded");
        return carRepos.saveAll(newCars);
    }

    @DeleteMapping("/cars/delete/{id}")  //deletes a car from the list based off of the id
    // This gets logged with a message of "{id} Data deleted".
    // So, put the id of the car that got deleted in the message itself.
    public ResponseEntity<?> deleteById(@PathVariable Long id)
    {
        carRepos.deleteById(id);

        CarLog message = new CarLog(id + "Data deleted");
        rt.convertAndSend(CarsApplication.QUEUE_NAME, message.toString());
        log.info(id + " Data deleted");
        return ResponseEntity.noContent().build();
    }

}
