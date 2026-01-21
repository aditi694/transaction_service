package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    @PostMapping
    public BeneficiaryResponse add(
            @RequestBody BeneficiaryRequest request
    ) {
        return null;
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable String id) {
        return "Verification initiated";
    }
}
