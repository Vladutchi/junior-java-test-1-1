package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    @Value("${insurance.validity-interval-years:50}")
    private int validityIntervalYears;

    public CarService(CarRepository carRepository,
                      InsurancePolicyRepository policyRepository,
                      ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public List<Claim> listClaims(Long carId) {
        if (carId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car id must be provided");
        }
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
        return claimRepository.findByCarIdOrderByClaimDateAsc(car.getId());
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be provided");
        }
        LocalDate minDate = LocalDate.now().minusYears(validityIntervalYears);
        LocalDate maxDate = LocalDate.now().plusYears(validityIntervalYears);

        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Date is outside the supported range: " + minDate + " to " + maxDate);
        }
        if (carId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car id must be provided");
        }
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        return policyRepository.existsActiveOnDate(car.getId(), date);
    }

    public Claim registerClaim(Long carId, LocalDate claimDate, String description, BigDecimal amount) {
        if (carId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car id must be provided");
        }
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        Claim claim = new Claim(car, claimDate, description, amount);
        return claimRepository.save(claim);
    }
}
