package com.logistock.InventoryManagmentSystem.services.imp;

import com.logistock.InventoryManagmentSystem.dtos.LoginRequest;
import com.logistock.InventoryManagmentSystem.dtos.RegisterRequest;
import com.logistock.InventoryManagmentSystem.dtos.Response;
import com.logistock.InventoryManagmentSystem.dtos.UserDTO;
import com.logistock.InventoryManagmentSystem.enums.UserRole;
import com.logistock.InventoryManagmentSystem.exceptions.InvalidCredentialsException;
import com.logistock.InventoryManagmentSystem.exceptions.NotFoundException;
import com.logistock.InventoryManagmentSystem.models.User;
import com.logistock.InventoryManagmentSystem.repositories.UserRepository;
import com.logistock.InventoryManagmentSystem.security.JwtUtils;
import com.logistock.InventoryManagmentSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;

    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        UserRole role = UserRole.MANAGER;

        if (registerRequest.getRole() != null) {
            role = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("User was successfully registered")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found."));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Password dos not match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .status(200)
                .message("User logged in successfully.")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 months")
                .build();
    }

    @Override
    public Response getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        users.forEach(user -> user.setTransaction(null));

        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));

        user.setTransaction(null);

        return user;
    }

    @Override
    public Response getUserById(Long id) {

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("User not found."));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.setTransaction(null);

        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("User successfully updated.")
                .user(userDTO)
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

        userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found."));

        userRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("User successfully deleted.")
                .build();
    }

    @Override
    public Response getUserTransactions(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.getTransaction().forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setSupplier(null);
        });

        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }
}
