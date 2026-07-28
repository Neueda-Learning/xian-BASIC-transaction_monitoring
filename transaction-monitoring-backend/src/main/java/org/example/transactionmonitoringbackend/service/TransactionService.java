package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Alert;
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
    @Autowired
    private AlertService alertService;

    public int addTransaction(Transaction transaction){
        int rule1 = fixedRules.checkSingleTransaction(transaction);
        int rule2 = fixedRules.checkWindow(transaction);
        int rule4 = fixedRules.dailyLimit(transaction);
        if(rule1==0 && rule2==0 && rule4==0 ){
            return transactionRepository.addTransaction(transaction);
        }
        if(rule1 == 1){
            System.out.println("rule1 alert");
            Alert alert1 = new Alert();
            alert1.setTransactionId(transaction.getId());
            alert1.setRuleId(1L);
            alertService.createAlert(alert1);
        }
        if(rule2 == 2){
            System.out.println("rule2 alert");
            Alert alert2 = new Alert();
            alert2.setTransactionId(transaction.getId());
            alert2.setRuleId(2L);
            alertService.createAlert(alert2);
        }
        if(rule4 == 4){
            System.out.println("rule4 alert");
            Alert alert4 = new Alert();
            alert4.setTransactionId(transaction.getId());
            alert4.setRuleId(4L);
            alertService.createAlert(alert4);
        }
        transactionRepository.addTransaction(transaction);
        return 0;
    }


    public List<Transaction> getAllTransactions() {
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.getTransactionById(id);
    }
}
