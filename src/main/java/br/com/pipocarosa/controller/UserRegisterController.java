package br.com.pipocarosa.controller;

import br.com.pipocarosa.authentication.AuthenticationResponse;
import br.com.pipocarosa.authentication.AuthenticationService;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class UserRegisterController {

    @Autowired
    private final UserRegisterService userRegisterService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/users")
    public ResponseEntity<List<UserRecordDto>> getAllUsers() {
        List<UserModel> users = userRepository.findAll();
        List<UserRecordDto> usersDto = users.stream()
                .map(user -> new UserRecordDto(
                        user.getName(),
                        user.getEmail(),
                        user.getBirthDate(),
                        user.getPassword())
                )
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(usersDto);
    }

    @PostMapping("/user")
    public ResponseEntity<AuthenticationResponse> saveUser(@RequestBody @Valid UserRecordDto userRecordDto) {
        userRegisterService.validateUser(userRecordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(userRecordDto));
    }
}
