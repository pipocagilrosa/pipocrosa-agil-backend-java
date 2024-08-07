package br.com.pipocarosa.services;

import br.com.pipocarosa.dtos.PasswordUpdateDto;
import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.dtos.UserUpdateDto;
import br.com.pipocarosa.exceptions.UserNotFoundException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserUpdateService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void deleteUser(UUID uuid) {

        Optional<UserModel> optionalUser = userRepository.findByUuid(uuid);

        if(optionalUser.isPresent()) {
            userRepository.deleteById(optionalUser.get().getId());
        } else {
            throw new UserNotFoundException();
        }
    }

    public void updateUser(UserUpdateDto userUpdateDto, UUID uuid){

        Optional<UserModel> optionalUser = userRepository.findByUuid(uuid);

        if(optionalUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        UserModel userFound = optionalUser.get();

        userFound.setName(userUpdateDto.name());
        userFound.setBirthDate(userUpdateDto.birthDate());

        userRepository.save(userFound);

    }

    public void updatePassword(PasswordUpdateDto passwordUpdateDto, UUID uuid) {
        Optional<UserModel> optionalUser = userRepository.findByUuid(uuid);

        if(optionalUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        UserModel userFound = optionalUser.get();

        userFound.setPassword(passwordEncoder.encode(passwordUpdateDto.password()));

        userRepository.save(userFound);
    }
}
