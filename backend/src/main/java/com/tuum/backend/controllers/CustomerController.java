package com.tuum.backend.controllers;

import com.tuum.backend.entities.Customer;
import com.tuum.backend.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tuum/api")
public class CustomerController {
    private final CustomerService customerService;
    @GetMapping(path = "/customers")
    public List<Customer> getCustomers() {
        return customerService.getCustomers();
    }
}
