package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.repository.TransactionRepository;
import org.example.transactionmonitoringbackend.service.MonitoringRuleService;
import org.example.transactionmonitoringbackend.service.FixedRule3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MonitoringRuleService monitoringRuleService;

    @Autowired
    private FixedRule3 fixedRule3;

    /**
     * Add a transaction while running simple monitoring rules.
     * If a monitoring rule detects an issue (e.g., missing payee),
     * we return -1 to indicate a warning and do not persist the transaction.
     */
    public int addTransaction(Transaction transaction){
        // run fixed rule 3: payee existence check
        int rule3 = fixedRule3.checkPayeeExistence(transaction);
        if (rule3 == 3) {
            // payee not found -> warning, do not persist
            return -1;
        }

        // Other rules can be executed here (amount threshold, velocity, etc.)

        return transactionRepository.addTransaction(transaction);

    }


    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.getTransactionById(id);
    }
}
