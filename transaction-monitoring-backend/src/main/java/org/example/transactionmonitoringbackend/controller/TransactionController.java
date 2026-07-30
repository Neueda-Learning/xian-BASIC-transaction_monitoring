package org.example.transactionmonitoringbackend.controller;

import jakarta.validation.Valid;
import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.exception.ValidationException;
import org.example.transactionmonitoringbackend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public String addTransaction(@Valid @RequestBody Transaction transaction){
        int result = transactionService.addTransaction(transaction);
        if(result == 1){
            return "add transaction success";
        }else{
            return "add transaction false";
        }
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/range-filter")
    public List<Transaction> filterByAmountAndTimeRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new ValidationException("minAmount must be <= maxAmount");
        }
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("startTime must be <= endTime");
        }

        return transactionService.filterByAmountAndTimeRange(minAmount, maxAmount, startTime, endTime);
    }
}
