package com.logistock.InventoryManagmentSystem.repositories;

import com.logistock.InventoryManagmentSystem.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
