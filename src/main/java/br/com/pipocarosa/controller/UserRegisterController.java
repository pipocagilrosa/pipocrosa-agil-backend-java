package br.com.pipocarosa.controller;

import br.com.pipocarosa.authentication.AuthenticationRequest;
import br.com.pipocarosa.authentication.AuthenticationResponse;
import br.com.pipocarosa.authentication.AuthenticationService;
import br.com.pipocarosa.dtos.PasswordUpdateDto;
import br.com.pipocarosa.dtos.UserIdentifierDto;
import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.dtos.UserUpdateDto;
import br.com.pipocarosa.exceptions.BusinessRulesException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserQueryService;
import br.com.pipocarosa.services.UserRegisterService;
import br.com.pipocarosa.services.UserUpdateService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class UserRegisterController {

    @Autowired
    private final UserRegisterService userRegisterService;

    @Autowired
    private final UserQueryService userQueryService;

    @Autowired
    private final UserUpdateService userUpdateService;

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

    @GetMapping("/user/{uuid}")
    public ResponseEntity<UserRecordDto> getUser(@PathVariable UUID uuid) {
        UserRecordDto userDto = userQueryService.getOneUser(uuid);
        return ResponseEntity.ok().body(userDto);
    }

    @DeleteMapping("/user/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        userUpdateService.deleteUser(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> saveUser(@RequestBody @Valid UserRecordDto userRecordDto) {
        userRegisterService.validateUser(userRecordDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(userRecordDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PutMapping("/user/{uuid}")
    public ResponseEntity<String> updateUser(
            @RequestBody @Valid UserUpdateDto userUpdateDto,
            @PathVariable UUID uuid
    ) {
        userUpdateService.updateUser(userUpdateDto, uuid);
        return ResponseEntity.status(HttpStatus.OK).body("User updated");
    }

    @PutMapping("/password/{uuid}")
    public ResponseEntity<String> updatePassword(
            @RequestBody @Valid PasswordUpdateDto passwordUpdateDto,
            @PathVariable UUID uuid
            ) {
        userUpdateService.updatePassword(passwordUpdateDto, uuid);
        return ResponseEntity.status(HttpStatus.OK).body("Password updated");
    }
}
