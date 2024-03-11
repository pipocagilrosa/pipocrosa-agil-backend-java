package br.com.pipocarosa.services;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.ExistingEmailException;
import br.com.pipocarosa.exceptions.YoungUserException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserRegisterService {
    //    @Autowired
    //    private UserRepository userRepository;

    String errorMessage = "Error in Business Rules";
    private final List<UserRecordDto> usersList = new ArrayList<UserRecordDto>();

    public void validateUser(UserRecordDto userRecordDto) {
        for (UserRecordDto user : usersList) {
            if (user.email().equals(userRecordDto.email())) {
                throw new ExistingEmailException(errorMessage);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate birthDate = LocalDate.parse(userRecordDto.birthDate(), formatter);
        LocalDate currentDate = LocalDate.now();
        Period interval = Period.between(birthDate, currentDate);
        if (interval.toTotalMonths() < 18 * 12) {
            throw new YoungUserException(errorMessage);
        }
        usersList.add(userRecordDto);
        System.out.println(usersList);
    }

//    public void validateUser(String email) {
//        if (userRepository.existsByEmail(email)) {
//            throw new ExistingEmailException("Email already exists");
//        }
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        LocalDate birthDate = LocalDate.parse(date, formatter);
//        LocalDate currentDate = LocalDate.now();
//        Period interval = Period.between(birthDate, currentDate);
//
//        if (interval.getYears() < 18) {
//            throw new YoungUserException("Underage user");
//        } else if (interval.getYears() == 18) {
//            if (interval.getMonths() < 0 || interval.getMonths() == 0 && interval.getDays() < 0) {
//                throw new YoungUserException("Underage user");
//            }
//        }
//        var userModel = new UserModel();
//        BeanUtils.copyProperties(userRecordDto, userModel);
//        userRepository.save(userModel);
//    }
}