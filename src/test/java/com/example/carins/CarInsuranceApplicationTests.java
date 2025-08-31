package com.example.carins;

import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.web.CarController;
import com.example.carins.web.dto.ClaimDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;
    @Autowired
    CarController controller;
    @Value("${insurance.validity-interval-years:50}")
    private int validityIntervalYears;

    // --- Task B tests: register claim + history ---

    @Test
    void registerClaim_success() {
        Claim saved = service.registerClaim(1L,
                LocalDate.now().minusDays(1),
                "Test accident",
                BigDecimal.valueOf(100.00)
        );
        assertNotNull(saved.getId());
        assertEquals("Test accident", saved.getDescription());
        assertEquals(1L, saved.getCar().getId());
    }

    @Test
    void registerClaim_nonExistingCar_throws404() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.registerClaim(9999L, LocalDate.now(), "desc", BigDecimal.TEN));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Car not found"));
    }

    @Test
    void getHistory_returnsChronological() {
        List<ClaimDto> history = controller.getCarHistory(1L);
        for (int i = 1; i < history.size(); i++) {
            LocalDate prev = history.get(i - 1).claimDate();
            LocalDate curr = history.get(i).claimDate();
            assertFalse(curr.isBefore(prev), "History must be sorted by claimDate ascending");
        }
    }

    // --- Task C tests: insurance-valid validation ---

    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void wrongFormatDate_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.isInsuranceValid(1L, "2025-29-01"));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Invalid date format"));
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
        LocalDate tooOld = LocalDate.now().minusYears(validityIntervalYears + 1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, tooOld));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("outside the supported range"));
    }

    @Test
    void dateAboveMax_throws400() {
        LocalDate tooFar = LocalDate.now().plusYears(validityIntervalYears + 1);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, tooFar));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("outside the supported range"));
    }

    @Test
    void nonExistingCar_throws404() {
        LocalDate today = LocalDate.now();
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(9999L, today));
        assertEquals(404, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Car not found"));
    }

    @Test
    void minBoundary_ok() {
        LocalDate minDate = LocalDate.now().minusYears(validityIntervalYears);
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, minDate));
    }

    @Test
    void maxBoundary_ok() {
        LocalDate maxDate = LocalDate.now().plusYears(validityIntervalYears);
        assertDoesNotThrow(() -> service.isInsuranceValid(1L, maxDate));
    }
}
