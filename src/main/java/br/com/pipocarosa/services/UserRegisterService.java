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
import java.util.Optional;

@Service
public class UserRegisterService {
    @Autowired
    private UserRepository userRepository;

    String errorMessage = "Error in Business Rules";

    public boolean checkAge(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = LocalDate.parse(date, formatter);
        LocalDate currentDate = LocalDate.now();
        Period interval = Period.between(birthDate, currentDate);
        if(interval.getYears() > 18) {
            return true;
        } else if(interval.getYears() == 18) {
            if(interval.getMonths() > 0) {
                return true;
            } else if(interval.getMonths() == 0) {
                return interval.getDays() >= 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void validateUser(UserRecordDto userRecordDto) {
        if (userRepository.existsByEmail(userRecordDto.email())) {
            throw new ExistingEmailException(errorMessage);
        }

        if (!checkAge(userRecordDto.birthDate())) {
            throw new YoungUserException(errorMessage);
        }
        var userModel = new UserModel();
        BeanUtils.copyProperties(userRecordDto, userModel);
        userRepository.save(userModel);
    }

}