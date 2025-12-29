package com.logistock.InventoryManagmentSystem.repositories;

import com.logistock.InventoryManagmentSystem.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
