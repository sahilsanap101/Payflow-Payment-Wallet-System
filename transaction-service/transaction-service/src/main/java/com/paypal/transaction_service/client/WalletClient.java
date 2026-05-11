package com.paypal.transaction_service.client;

import com.paypal.transaction_service.dto.CaptureRequest;
import com.paypal.transaction_service.dto.CreditRequest;
import com.paypal.transaction_service.dto.DebitRequest;
import com.paypal.transaction_service.dto.HoldRequest;
import com.paypal.transaction_service.dto.HoldResponse;
import com.paypal.transaction_service.dto.WalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "wallet-service",
        url = "${services.wallet.base-url:http://localhost:8083}/api/v1/wallets"
)
public interface WalletClient {

    @PostMapping(value = "/debit", consumes = "application/json", produces = "application/json")
    WalletResponse debit(@RequestBody DebitRequest request);

    @PostMapping(value = "/credit", consumes = "application/json", produces = "application/json")
    WalletResponse credit(@RequestBody CreditRequest request);

    @PostMapping(value = "/hold", consumes = "application/json", produces = "application/json")
    HoldResponse placeHold(@RequestBody HoldRequest request);

    @PostMapping(value = "/capture", consumes = "application/json", produces = "application/json")
    WalletResponse capture(@RequestBody CaptureRequest request);

    @PostMapping(value = "/release/{holdReference}", produces = "application/json")
    HoldResponse release(@PathVariable("holdReference") String holdReference);

    @GetMapping(value = "/{userId}", produces = "application/json")
    WalletResponse getWallet(@PathVariable("userId") Long userId);
}