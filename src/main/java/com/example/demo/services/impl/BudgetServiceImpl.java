package com.example.demo.services.impl;

import com.example.demo.model.Category;
import com.example.demo.model.Transaction;
import com.example.demo.services.BudgetService;
import com.example.demo.services.FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Service
public class BudgetServiceImpl implements BudgetService {
    public static final int SALARY = 20_000;
    public static final int AVG_SALARY = SALARY;
    public static final double AVG_DAYS = 29.3;
    public static final int SAVING = 3000;
    public static final int DAILY_BUDGET = (SALARY - SAVING) / LocalDate.now().lengthOfMonth();
    public static final int BALANCE = 0;

    private static TreeMap<Month, LinkedHashMap<Long, Transaction>> transactions = new TreeMap<>();
    private static long lastId = 0;

    private FileService fileService;

    public BudgetServiceImpl(FileService fileService) {
        this.fileService = fileService;
    }
    @PostConstruct
    private void init() {
        readFromFile();
    }
    @Override
    public int getDailyBudget() {
        return DAILY_BUDGET;
    }

    @Override
    public int getBalance() {
        return SALARY - SAVING - getAllSpend();
    }

    @Override
    public long addTransaction(Transaction transaction) {
        LinkedHashMap<Long, Transaction> monthTransaction = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<Long, Transaction>());
        monthTransaction.put(lastId, transaction);
        transactions.put(LocalDate.now().getMonth(), monthTransaction);
        saveToFile();
        return lastId++;
    }

    @Override
    public Transaction getTransaction(long id) {
        for (LinkedHashMap<Long, Transaction> transactionByMonth : transactions.values()) {
            Transaction transaction = transactionByMonth.get(id);
            if (transaction != null) {
                return transaction;
            }
        }
        return null;

    }

    @Override
    public LinkedHashMap<Long, Transaction> getAllTransactions(Month month) {
        if (transactions.containsKey(month)) {
            return transactions.get(month);
        }
        return null;
    }

    @Override
    public LinkedHashMap<Long, Transaction> getAllTransactions(Category category) {
        LinkedHashMap<Long,Transaction> transactionsByCategory = new LinkedHashMap<>();
        long id = 0;
        for (LinkedHashMap<Long, Transaction> transactionMap : transactions.values()) {
            for (Transaction transaction : transactionMap.values()) {
                if (transaction.getCategory().equals(category)) {
                    transactionsByCategory.put(id++, transaction);
                }
            }
        }
        return transactionsByCategory;
    }

    @Override
    public Map<Long, Transaction> getAllTransactions(Month month, Category category) {
        if (transactions.containsKey(month)) {
            return getAllTransactions(category);
        }
        return null;
    }
    @Override
    public Transaction editTransaction(long id, Transaction transaction) {
        for (Map<Long, Transaction> transactionByMonth : transactions.values()) {
            if (transactionByMonth.containsKey(id)) {
                transactionByMonth.put(id, transaction);
                saveToFile();
//                transactions.put(LocalDate.now().getMonth(), transactionByMonth);
                return transaction;
            }
        }
        return null;
    }

    @Override
    public boolean deleteTransaction(long id) {
        for (Map<Long, Transaction> transactionByMonth : transactions.values()) {
            if (transactionByMonth.containsKey(id)) {
                transactionByMonth.remove(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteAllTransactions() {
        transactions = new TreeMap<>();
    }

    @Override
    public int getDailyBalance() {
        return DAILY_BUDGET * LocalDate.now().getDayOfMonth() - getAllSpend();

    }

    @Override
    public int getAllSpend() {
        Map<Long, Transaction> monthTransaction = transactions.getOrDefault(LocalDate.now().getMonth(), new LinkedHashMap<Long, Transaction>());
        int sum = 0;
        for (Transaction transaction : monthTransaction.values()) {
            sum += transaction.getSum();
        }
        return sum;
    }

    @Override
    public int getVacationBonus(int daysCount) {
        double averageDaySalary = AVG_SALARY / AVG_DAYS;
        return (int) (daysCount * averageDaySalary);
    }

    @Override
    public int getSalaryWithVacation(int vacationDaysCount, int workingDaysCount, int workingDaysInMonth) {

        int salary = SALARY / workingDaysInMonth * (workingDaysInMonth - workingDaysCount);
        return salary + getVacationBonus(vacationDaysCount);
    }
    @Override
    public Path createMonthlyReport(Month month) throws IOException {
        Path path = fileService.createTempFile("report");
        LinkedHashMap<Long, Transaction> monthlyTransactions = transactions.getOrDefault(month, new LinkedHashMap<>());
        for (Transaction transaction : monthlyTransactions.values()) {
            try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.append(transaction.getCategory() + ": "
                        + transaction.getSum() +
                        " руб. -  " + transaction.getComment());
                writer.append('\n');
            }
        }
        return path;
    }
    @Override
    public void addTransactionsFromInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader((new InputStreamReader((inputStream))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] array = StringUtils.split(line, '|');
                Transaction transaction = new Transaction(Category.valueOf(array[0]), Integer.valueOf(1), array[2]);
                addTransaction(transaction);
            }
        }
    }
    private void saveToFile() {
        try {
            String json = new ObjectMapper().writeValueAsString(transactions);
            fileService.saveToFile(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFromFile() {
        String json = fileService.readFromFile();
        try {
            transactions = new ObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
