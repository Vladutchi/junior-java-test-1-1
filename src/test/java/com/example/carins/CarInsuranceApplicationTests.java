package com.example.carins;

import com.example.carins.service.CarService;
import com.example.carins.web.CarController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;
    @Autowired
    CarController controller;

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    // --- Task C tests: invalid inputs ---

    @Test
    void wrongFormatDate_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.isInsuranceValid(1L, "2025/02/01"));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Invalid date format. Use YYYY-MM-DD."));
    }

    @Test
    void nullCarId_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(null, LocalDate.now()));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Car id must be provided"));
    }

    @Test
    void nullDate_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, null));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Date must be provided"));
    }

    @Test
    void dateBelowMin_throws400() {
        LocalDate tooOld = LocalDate.now().minusYears(51);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, tooOld));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Date is outside the supported range"));
    }

    @Test
    void dateAboveMax_throws400() {
        LocalDate tooFar = LocalDate.now().plusYears(51);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, tooFar));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Date is outside the supported range"));
    }

    @Test
    void nonExistingCar_throws404() {
        LocalDate today = LocalDate.now();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(9999L, today));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Car"));
    }

    @Test
    void minBoundary_ok() {
        LocalDate minDate = LocalDate.now().minusYears(50);
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, minDate));
    }

    @Test
    void maxBoundary_ok() {
        LocalDate maxDate = LocalDate.now().plusYears(50);
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, maxDate));
    }
}
