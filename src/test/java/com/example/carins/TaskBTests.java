package com.example.carins;

import com.example.carins.model.Claim;
import com.example.carins.service.CarService;
import com.example.carins.web.CarController;
import com.example.carins.web.dto.ClaimDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class TaskBTests {
    @Autowired
    CarService service;
    @Autowired
    CarController controller;

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
            assertFalse(curr.isBefore(prev));
        }
    }
}
