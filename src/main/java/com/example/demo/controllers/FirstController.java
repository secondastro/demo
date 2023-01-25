package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FirstController {
    @GetMapping
    public String helloWeb() {
        return "342342";
    }

    @GetMapping("/path")
    public String page(@RequestParam String page) {
        return "tret" + page;
    }
}
