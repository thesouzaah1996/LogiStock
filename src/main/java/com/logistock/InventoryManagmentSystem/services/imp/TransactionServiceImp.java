package com.logistock.InventoryManagmentSystem.services.imp;

import com.logistock.InventoryManagmentSystem.dtos.Response;
import com.logistock.InventoryManagmentSystem.dtos.SupplierDTO;
import com.logistock.InventoryManagmentSystem.dtos.TransactionDTO;
import com.logistock.InventoryManagmentSystem.dtos.TransactionRequest;
import com.logistock.InventoryManagmentSystem.enums.TransactionStatus;
import com.logistock.InventoryManagmentSystem.enums.TransactionType;
import com.logistock.InventoryManagmentSystem.exceptions.NameValueRequiredException;
import com.logistock.InventoryManagmentSystem.exceptions.NotFoundException;
import com.logistock.InventoryManagmentSystem.models.Product;
import com.logistock.InventoryManagmentSystem.models.Supplier;
import com.logistock.InventoryManagmentSystem.models.Transaction;
import com.logistock.InventoryManagmentSystem.models.User;
import com.logistock.InventoryManagmentSystem.repositories.ProductRepository;
import com.logistock.InventoryManagmentSystem.repositories.SupplierRepository;
import com.logistock.InventoryManagmentSystem.repositories.TransactionRepository;
import com.logistock.InventoryManagmentSystem.services.TransactionService;
import com.logistock.InventoryManagmentSystem.services.UserService;
import com.logistock.InventoryManagmentSystem.specification.TransactionFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImp implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response purchase(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id is required");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Supplier supplier  = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found"));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message("Purchase made successfully")
                .build();
    }

    @Override
    public Response sell(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();

        Integer quantity = transactionRequest.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message("Product sale successfully made")
                .build();
    }

    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id is required");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Supplier supplier  = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found"));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity());
        productRepository.save(product);

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .status(200)
                .message("Product returned in progress")
                .build();

    }

    @Override
    public Response getAllTransactions() {

        List<Transaction> transactions = transactionRepository.findAll();

        transactions.forEach(transaction -> {
            transaction.setUser(null);
        });

        List<TransactionDTO> transactionDTOS = modelMapper.map(transactions, new TypeToken<List<TransactionDTO>>() {
        }.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .transactions(transactionDTOS)
                .build();
    }

    @Override
    public Response getTransactionsById(Long id) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);

        transactionDTO.getUser().setTransaction(null);

        return Response.builder()
                .status(200)
                .message("success")
                .transaction(transactionDTO)
                .build();
    }


    @Override
    public Response updateTransactionStatus(Long transactionId, TransactionStatus status) {

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        existingTransaction.setStatus(status);
        existingTransaction.setUpdateAt(LocalDateTime.now());

        transactionRepository.save(existingTransaction);

        return Response.builder()
                .status(200)
                .message("Transaction status successfully updated")
                .build();
    }
}
