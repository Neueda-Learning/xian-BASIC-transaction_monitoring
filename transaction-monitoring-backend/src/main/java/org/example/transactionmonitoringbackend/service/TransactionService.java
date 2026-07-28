package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FixedRules fixedRules;

    public int addTransaction(Transaction transaction){
        int rule1 = fixedRules.checkSingleTransaction(transaction);
        int rule2 = fixedRules.checkWindow(transaction);
        int rule3 = fixedRules.checkFirstTransactionToPayee(transaction);
        int rule4 = fixedRules.dailyLimit(transaction);

    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.getTransactionById(id);
    }
}
