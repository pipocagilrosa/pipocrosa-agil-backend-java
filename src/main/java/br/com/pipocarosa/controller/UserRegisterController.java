package br.com.pipocarosa.controller;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserRegisterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class UserRegisterController {

    @Autowired
    private final UserRegisterService userRegisterService;

//    @Autowired
//    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<String> executeCall() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello world");
    }

    @PostMapping("/users")
    public ResponseEntity<String> testUser(@RequestBody @Valid UserRecordDto userRecordDto) {
            userRegisterService.validateUser(userRecordDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Valid user");
    }

//    @PostMapping("/users")
//    public ResponseEntity<String> saveUser(@RequestBody @Valid UserRecordDto userRecordDto) {
//        userRegisterService.validateUser(userRecordDto);
//        return ResponseEntity.status(HttpStatus.CREATED);
//    }
}
