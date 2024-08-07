package br.com.pipocarosa.services;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.UserNotFoundException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserQueryService {

    @Autowired
    private UserRepository userRepository;

    public UserRecordDto getOneUser(UUID uuid) {

        Optional<UserModel> optionalUser = userRepository.findByUuid(uuid);

        if(optionalUser.isPresent()) {
            UserModel user = optionalUser.get();
            return new UserRecordDto(
                    user.getName(),
                    user.getEmail(),
                    user.getBirthDate(),
                    user.getPassword()
            );
        } else {
            throw new UserNotFoundException();
        }
    }

    public Optional<UserModel> findUserByUuid(UUID uuid) {
        return userRepository.findByUuid(uuid);
    }

    public Optional<UserModel> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
