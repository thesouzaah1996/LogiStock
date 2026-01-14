package com.logistock.InventoryManagmentSystem.services;

import com.logistock.InventoryManagmentSystem.dtos.Response;
import com.logistock.InventoryManagmentSystem.dtos.TransactionRequest;
import com.logistock.InventoryManagmentSystem.enums.TransactionStatus;

public interface TransactionService {

    Response purchase(TransactionRequest transactionRequest);

    Response sell(TransactionRequest transactionRequest);

    Response returnToSupplier(TransactionRequest transactionRequest);

    Response getAllTransactions();

    Response getTransactionsById(Long id);

    Response updateTransactionStatus(Long transactionId, TransactionStatus status);
}
