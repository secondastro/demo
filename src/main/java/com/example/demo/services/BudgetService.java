package com.example.demo.services;

import com.example.demo.model.Category;
import com.example.demo.model.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Month;
import java.util.Map;

public interface BudgetService {

    int getDailyBudget();
    int getBalance();

    long addTransaction(Transaction transaction);

    Transaction getTransaction(long id);

    Map<Long, Transaction> getAllTransactions(Month month, Category category);

    Map<Long, Transaction> getAllTransactions(Month month);

    Map<Long, Transaction> getAllTransactions(Category category);

    Transaction editTransaction(long id, Transaction transaction);

    boolean deleteTransaction(long id);

    void deleteAllTransactions();

    int getDailyBalance();

    int getAllSpend();

    int getVacationBonus(int daysCount);

    int getSalaryWithVacation(int vacationDaysCount, int workingDaysCount, int workingDaysInMonth);

    Path createMonthlyReport(Month month) throws IOException;

    void addTransactionsFromInputStream(InputStream inputStream) throws IOException;
}
