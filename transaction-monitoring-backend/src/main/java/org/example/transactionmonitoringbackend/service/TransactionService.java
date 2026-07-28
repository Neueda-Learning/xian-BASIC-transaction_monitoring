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

        // If rule3 returns 5 it means the payee was not found in users table.
        // We treat this as a warning and do not persist the transaction.
        if (rule3 == 5) {
            System.out.println("warning: payee not found, aborting insert");
            return -1;
        }

        System.out.println("rule1:" +rule1);

        return transactionRepository.addTransaction(transaction);

    }


    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.getTransactionById(id);
    }
}
