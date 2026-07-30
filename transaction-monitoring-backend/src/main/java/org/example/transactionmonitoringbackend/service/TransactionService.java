package org.example.transactionmonitoringbackend.service;

import jakarta.transaction.Transactional;
import org.example.transactionmonitoringbackend.entity.Alert;
import org.example.transactionmonitoringbackend.entity.AlertSeverity;
import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.exception.TransactionNotFoundException;
import org.example.transactionmonitoringbackend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FixedRules fixedRules;
    @Autowired
    private AlertService alertService;

    /*
     * Apply all fixed rules before save.
     * If no rule is triggered, save as normal transaction.
     * If any rule is triggered, save with Alert status and create one alert per rule:
     * 1 = amount threshold, 2 = velocity, 3 = unknown payee, 4 = daily limit.
     */
    @Transactional
    public int addTransaction(Transaction transaction){
        if(transaction.getAmount().compareTo(BigDecimal.ZERO) < 0){
            return 0;
        }
        int rule1 = fixedRules.checkSingleTransaction(transaction);
        int rule2 = fixedRules.checkWindow(transaction);
        int rule3 = fixedRules.checkFirstTransactionToPayee(transaction);
        int rule4 = fixedRules.dailyLimit(transaction);
        if(rule1==0 && rule2==0 && rule3==0 && rule4==0 ){
            return transactionRepository.addTransaction(transaction);
        }
        transaction.setStatus("Alert");
        Transaction savedTransaction = transactionRepository.save(transaction);
        Long transactionid = savedTransaction.getId();

        if(rule1 == 1){
            Alert alert1 = new Alert();
            alert1.setTransactionId(transactionid);
            alert1.setRuleId(1L);
            alertService.createAlert(alert1, AlertSeverity.LOW);
        }
        if(rule2 == 2){
            Alert alert2 = new Alert();
            alert2.setTransactionId(transactionid);
            alert2.setRuleId(2L);
            alertService.createAlert(alert2,AlertSeverity.LOW);
        }
        if(rule3 == 3){
            Alert alert3 = new Alert();
            alert3.setTransactionId(transactionid);
            alert3.setRuleId(3L);
            alertService.createAlert(alert3,AlertSeverity.MEDIUM);
        }
        if(rule4 == 4){
            Alert alert4 = new Alert();
            alert4.setTransactionId(transactionid);
            alert4.setRuleId(4L);
            alertService.createAlert(alert4,AlertSeverity.HIGH);
        }

        return 0;
    }


    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        try {
            return transactionRepository.getTransactionById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new TransactionNotFoundException("Transaction not found");
        }
    }

    public List<Transaction> filterByAmountAndTimeRange(BigDecimal minAmount,
                                                        BigDecimal maxAmount,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime) {
        return transactionRepository.filterByAmountAndTimeRange(minAmount, maxAmount, startTime, endTime);
    }
}
