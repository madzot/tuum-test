package com.tuum.backend.services;

import com.tuum.backend.entities.Customer;
import com.tuum.backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Service("CustomerService")
@RequiredArgsConstructor
@RequestMapping("/tuum/api")
public class CustomerService {

    private final CustomerRepository customerRepository;
    public List<Customer> getCustomers() {
        return customerRepository.findAll();
    }
}
