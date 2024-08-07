package br.com.pipocarosa.authentication;

import br.com.pipocarosa.config.JwtService;
import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.UserAlreadyExistsException;
import br.com.pipocarosa.exceptions.UserNotFoundException;
import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.models.enums.Role;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserQueryService userQueryService;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticationResponse register(UserRecordDto request) {

        var user = new UserModel();
        user.setUuid(UUID.randomUUID());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setBirthDate(request.birthDate());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);

        String token = jwtService.generateToken(user, generateExtraClaims(user));
        UUID uuid = user.getUuid();

        return new AuthenticationResponse(token, uuid);
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest) {

        Optional<UserModel> optionalUser = userRepository.findByEmail(authenticationRequest.getEmail());

        if(optionalUser.isEmpty()) {
            throw new UserNotFoundException();
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(), authenticationRequest.getPassword()
        );

        authenticationManager.authenticate(authToken);

        UserModel user = optionalUser.get();

        String jwt = jwtService.generateToken(user, generateExtraClaims(user));
        UUID uuid = user.getUuid();

        return new AuthenticationResponse(jwt, uuid);
    }

    private Map<String, Object> generateExtraClaims(UserModel user) {

        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("name", user.getName());
        extraClaims.put("role", user.getRole().name());

        return extraClaims;
    }
}
