package com.example.demo.controllers;

import com.example.demo.model.Category;
import com.example.demo.model.Transaction;
import com.example.demo.services.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.Map;

@RestController
@RequestMapping("/transaction")
@Tag(name = "Транзакции", description = "операции CRUD и другие эндпоинты")
public class TransactionController {
    private final BudgetService budgetService;

    public TransactionController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }


    @PostMapping("/add")
    public ResponseEntity<Long> addTransaction(@RequestBody Transaction transaction) {
        long id = budgetService.addTransaction(transaction);
        return ResponseEntity.ok().body(id);
    }

    @GetMapping
    @Operation(
            summary = "получение транзакции по месяцу и категории"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "transaction found",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Transaction.class)

                                    )
                            )

                    }

            )
    })
    public Map<Long, Transaction> getAllTransactions(@RequestParam(required = false) Month month,
                                                     @RequestParam(required = false) Category category) {
        return budgetService.getAllTransactions(month, category);

    }

    @GetMapping("/byMonth/{month}")
    public ResponseEntity<Object> getTransactionByMonth(@PathVariable Month month) {
        try {
            Path path = budgetService.createMonthlyReport(month);
            if (Files.size(path) == 0) {
                return ResponseEntity.noContent().build();
            }
            InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + month + " - report.txt\"")
                    .contentLength(Files.size(path))
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }

    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> addTransactionFromFile(@RequestParam MultipartFile file) {
        try(InputStream stream = file.getInputStream()) {
            budgetService.addTransactionsFromInputStream(stream);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
           e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable long id) {

        Transaction transaction = budgetService.getTransaction(id);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Transaction> editTransaction(@PathVariable long id, @RequestBody Transaction transaction) {
        if (budgetService.getTransaction(id) == null) {
            return ResponseEntity.notFound().build();
        }
        budgetService.editTransaction(id, transaction);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable long id) {
        if (!budgetService.deleteTransaction(id)) {
            return ResponseEntity.notFound().build();
        }
        budgetService.deleteTransaction(id);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllTransactions() {
        budgetService.deleteAllTransactions();
        return ResponseEntity.ok().build();
    }
}
