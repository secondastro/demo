package com.example.demo.controllers;

import com.example.demo.services.BudgetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vacation")
public class VacationController {
    private final BudgetService budgetService;

    public VacationController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/bonus")
    public int vacationBonus(@RequestParam int vacationDays){
        return budgetService.getVacationBonus(vacationDays);
    }
    @GetMapping("/salary")
    public int salaryWithVacation(@RequestParam int vacDays, @RequestParam int workDays, @RequestParam int monthWorkDays) {

        return budgetService.getSalaryWithVacation(vacDays, workDays, monthWorkDays);
    }
}
