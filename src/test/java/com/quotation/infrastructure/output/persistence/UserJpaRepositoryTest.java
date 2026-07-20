package com.quotation.infrastructure.output.persistence;

import com.quotation.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void save_WhenValidUser_ThenPersistedWithId() {
        User user = buildUser("Alice", "alice@example.com");

        User saved = userJpaRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findById_WhenExists_ThenReturnsUser() {
        User saved = userJpaRepository.save(buildUser("Bob", "bob@example.com"));

        Optional<User> result = userJpaRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bob");
    }

    @Test
    void findById_WhenNotExists_ThenReturnsEmpty() {
        Optional<User> result = userJpaRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_WhenEmailExists_ThenTrue() {
        userJpaRepository.save(buildUser("Carol", "carol@example.com"));

        boolean exists = userJpaRepository.existsByEmail("carol@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ThenFalse() {
        boolean exists = userJpaRepository.existsByEmail("nobody@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void findAll_WhenMultipleUsers_ThenReturnsAll() {
        userJpaRepository.save(buildUser("Dave", "dave@example.com"));
        userJpaRepository.save(buildUser("Eve", "eve@example.com"));

        List<User> users = userJpaRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteById_WhenExists_ThenUserRemoved() {
        User saved = userJpaRepository.save(buildUser("Frank", "frank@example.com"));

        userJpaRepository.deleteById(saved.getId());

        assertThat(userJpaRepository.findById(saved.getId())).isEmpty();
    }

    private User buildUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .phone("600000000")
                .build();
    }
}
