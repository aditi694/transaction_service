package com.bank.transaction_service.dto.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Configuration
@EnableFeignClients(basePackages = "com.bank.transaction_service.client")
public class FeignConfig {
}
