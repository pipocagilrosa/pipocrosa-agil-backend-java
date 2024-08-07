package br.com.pipocarosa.repositories;

import br.com.pipocarosa.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    boolean existsByEmail(String email);
    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByUuid(UUID uuid);

}
