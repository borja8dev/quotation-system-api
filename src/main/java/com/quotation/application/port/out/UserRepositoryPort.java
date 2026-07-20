package com.quotation.application.port.out;

import com.quotation.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    Optional<User> findByEmail(String email);
}
