package org.example.transactionmonitoringbackend.repository;

import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//JPA (Java Persistence API) repository for Alert entity
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(AlertStatus status);
}
