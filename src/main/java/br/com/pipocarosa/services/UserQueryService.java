package br.com.pipocarosa.services;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.BusinessRulesException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryService {

    @Autowired
    private UserRepository userRepository;

    public UserRecordDto getOneUser(String email) {

        Optional<UserModel> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()) {
            UserModel user = optionalUser.get();
            return new UserRecordDto(
                    user.getName(),
                    user.getEmail(),
                    user.getBirthDate(),
                    user.getPassword()
            );
        } else {
            throw new BusinessRulesException();
        }
    }
}
