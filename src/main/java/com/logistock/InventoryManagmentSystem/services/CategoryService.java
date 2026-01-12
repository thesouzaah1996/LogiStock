package com.logistock.InventoryManagmentSystem.services;

import com.logistock.InventoryManagmentSystem.dtos.CategoryDTO;
import com.logistock.InventoryManagmentSystem.dtos.Response;

public interface CategoryService {

    Response createCategory(CategoryDTO categoryDTO);

    Response getAllCategories();

    Response getCategoryById(Long id);

    Response updateCategory(Long id, CategoryDTO categoryDTO);

    Response deleteCategory(Long id);
}
