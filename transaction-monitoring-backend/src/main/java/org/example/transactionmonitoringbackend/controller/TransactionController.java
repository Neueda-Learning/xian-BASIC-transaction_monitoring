package org.example.transactionmonitoringbackend.controller;

import jakarta.validation.Valid;
import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
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

}
