package com.shubham.chatsapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class healthcheck {

    @GetMapping("/health")
    public String healthcheck(){
        return "HEllloo";
    }
}
