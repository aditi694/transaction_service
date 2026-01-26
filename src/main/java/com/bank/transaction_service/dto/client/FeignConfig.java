package com.bank.transaction_service.dto.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.bank.transaction_service.dto.client")
public class FeignConfig {
}