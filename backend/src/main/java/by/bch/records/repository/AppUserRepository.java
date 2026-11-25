package by.bch.records.repository;

import by.bch.records.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Для аутентификации (ТЗ В.5). */
    Optional<AppUser> findByLogin(String login);
}
