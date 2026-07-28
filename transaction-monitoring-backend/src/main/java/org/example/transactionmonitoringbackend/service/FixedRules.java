package org.example.transactionmonitoringbackend.service;

import org.example.transactionmonitoringbackend.entity.Transaction;
import org.example.transactionmonitoringbackend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class FixedRules {

    //Fixed demo thresholds from the training document.
    private static final BigDecimal FIXED_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal FIXED_DAILY_LIMIT = new BigDecimal("50000");
    private static final int FIXED_VELOCITY_COUNT_THRESHOLD = 5;
    private static final int FIXED_VELOCITY_WINDOW_MINUTES = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final TransactionRepository transactionRepository;

    public FixedRules(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    private Instant toInstantOrNow(Object transactionTime){
        if(transactionTime instanceof Instant instant){
            return instant;
        }
        if(transactionTime instanceof LocalDateTime localDateTime){
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        return Instant.now();
    }

    //rule 1: single transaction amount exceeds threshold.
    public int checkSingleTransaction(Transaction transaction) {
        if (transaction.getAmount() != null
                && transaction.getAmount().compareTo(FIXED_AMOUNT_THRESHOLD) > 0) {
            System.out.println("AMOUNT_THRESHOLD");
            return 1;
        }else{
            return 0;
        }
    }

    //rule 2 : velocity check within the trailing N-minute window.
    public int checkWindow(Transaction transaction) {
        //1.Determine the transaction time now
        Instant txTime = toInstantOrNow(transaction.getTransTimestamp());
        //2.Calculate the starting  point of the window
        Instant velocityStartTime = txTime.minusSeconds(FIXED_VELOCITY_WINDOW_MINUTES * 60L);
        //3.Query the number of transactions for the same account within the window
        long recentCount = transactionRepository.countByAccountIdAndTransactionTimeBetween(
                transaction.getAccountId(), velocityStartTime, txTime
        );
        //4.Comparison with threshold
        if (recentCount + 1 > FIXED_VELOCITY_COUNT_THRESHOLD) {
            System.out.println("VELOCITY_RULE");
            return 2;
        }else{
            return 0;
        }
    }

    //rule 3: first transaction to a payee for the same account.


    //rule 4: UTC day bucket cumulative amount check.
    public int dailyLimit(Transaction transaction){
        Instant txTime = toInstantOrNow(transaction.getTransTimestamp());
        Instant dayStart = LocalDate.ofInstant(txTime, ZoneOffset.UTC)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);
        Instant dayEnd = dayStart.plusSeconds(24*60*60L);
        BigDecimal dailyTotal = transactionRepository.sumAmountByAccountIdAndTransactionTimeBetween(
                transaction.getAccountId(),
                dayStart,
                dayEnd);
        BigDecimal projectedDailyTotal = dailyTotal.add(transaction.getAmount());
        if(projectedDailyTotal.compareTo(FIXED_DAILY_LIMIT) > 0){
            System.out.println("DAILY_LIMIT_RULE");
            return 4;
        }else{
            return 0;
        }
    }
}
