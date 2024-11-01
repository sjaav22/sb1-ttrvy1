package com.example.tasko.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/404")
    public String handleNotFound() {
        return "error/404";
    }

    @GetMapping("/500")
    public String handleServerError() {
        return "error/500";
    }
}