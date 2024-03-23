package br.com.pipocarosa.services;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.ExistingEmailException;
import br.com.pipocarosa.exceptions.YoungUserException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserRegisterService {
        @Autowired
        private UserRepository userRepository;

    String errorMessage = "Error in Business Rules";

    public void validateUser(UserRecordDto userRecordDto) {
        if (userRepository.existsByEmail(userRecordDto.email())) {
            throw new ExistingEmailException(errorMessage);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = LocalDate.parse(userRecordDto.birthDate(), formatter);
        LocalDate currentDate = LocalDate.now();
        Period interval = Period.between(birthDate, currentDate);
        if (interval.toTotalMonths() < 18 * 12) {
            throw new YoungUserException(errorMessage);
        }
        var userModel = new UserModel();
        BeanUtils.copyProperties(userRecordDto, userModel);
        userRepository.save(userModel);
    }


}