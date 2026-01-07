package com.logistock.InventoryManagmentSystem.services;

import com.logistock.InventoryManagmentSystem.dtos.LoginRequest;
import com.logistock.InventoryManagmentSystem.dtos.RegisterRequest;
import com.logistock.InventoryManagmentSystem.dtos.Response;
import com.logistock.InventoryManagmentSystem.dtos.UserDTO;
import com.logistock.InventoryManagmentSystem.models.User;
import jakarta.servlet.Registration;

public interface UserService {

    Response registerUser(RegisterRequest registerRequest);

    Response loginUser(LoginRequest loginRequest);

    Response getAllUsers();

    User getCurrentLoggedInUser();

    Response getUserById(Long id);

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    Response getUserTransactions(Long id);
}
