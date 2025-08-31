package com.example.carins.web;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PolicyExpiryLogger {

    private static final Logger log = LoggerFactory.getLogger(PolicyExpiryLogger.class);
    private final InsurancePolicyRepository repo;

    public PolicyExpiryLogger(InsurancePolicyRepository repo) {
        this.repo = repo;
    }

    @Scheduled(cron = "0 0 0 * * *") //Runs at midnight and checks policies that expired the previous day.
    public void logYesterdayExpiredPolicies() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<InsurancePolicy> expired = repo.findByEndDate(yesterday);

        for (var p : expired) {
            Long carId = (p.getCar() != null ? p.getCar().getId() : null);
            log.info("Policy {} for car {} expired on {}", p.getId(), carId, p.getEndDate());
        }
    }
}

