package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
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
        return claimRepository.findByCarIdOrderByClaimDateAsc(carId);
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        final int validityInterval = 50; //today ±50 years, adjust according to business standards
        LocalDate minDate = LocalDate.now().minusYears(validityInterval);
        LocalDate maxDate = LocalDate.now().plusYears(validityInterval);

        if (carId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car id must be provided");
        }
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be provided");
        } else if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is outside the supported range: " + minDate + " to " + maxDate);
        }
        if (!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
        }
        return policyRepository.existsActiveOnDate(carId, date);
    }
}
