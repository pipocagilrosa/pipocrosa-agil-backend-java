package br.com.pipocarosa;

import br.com.pipocarosa.dtos.UserRecordDto;
import br.com.pipocarosa.exceptions.ExistingEmailException;
import br.com.pipocarosa.models.UserModel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserRegisterService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRegisterServiceTest {

    @LocalServerPort
    private Integer port;
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16.2-alpine"
    );

    @BeforeAll
    static void startContainers() {
        postgres.start();
    }

    @AfterAll
    static void stopContainers() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRegisterService userRegisterService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        userRepository.deleteAll();
    }

    @Test
    void shouldVerifyIfUserIsNotUnderageWithSuccess() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate currentDate = LocalDate.now();
        LocalDate eighteenYearsAgoAndOneDayLaterDate = currentDate.minusYears(18).plusDays(1);
        LocalDate eighteenYearsAgoDate = currentDate.minusYears(18);

        String eighteenYearsAgoAndOneDayLater = eighteenYearsAgoAndOneDayLaterDate.format(formatter);
        String eighteenYearsAgo = eighteenYearsAgoDate.format(formatter);

        assertTrue(userRegisterService.checkAge(eighteenYearsAgo));
        assertFalse(userRegisterService.checkAge(eighteenYearsAgoAndOneDayLater));
    }

    @Test
    void shouldVerifyIfEmailAlreadyExists(){
        UserModel user = new UserModel(
                1L,
                "Junior Souza",
                "junior@gmail.com",
                "17/08/2001"
        );
        user.setPassword(passwordEncoder.encode("123456"));

        userRepository.save(user);
        boolean existingEmail = userRepository.existsByEmail("junior@gmail.com");
        boolean notExistingEmail = userRepository.existsByEmail("junior1@gmail.com");
        assertTrue(existingEmail);
        assertFalse(notExistingEmail);
    }

    @Test
    void shouldVerifyIfEmailNotExists(){
        UserModel user = new UserModel(
                1L,
                "Junior Souza",
                "junior@gmail.com",
                "17/08/2001"
        );
        user.setPassword(passwordEncoder.encode("123456"));

        userRepository.save(user);
        boolean notExistingEmail = userRepository.existsByEmail("junior1@gmail.com");
        assertFalse(notExistingEmail);
    }

    @Test
    void shouldPostUser() {
        String requestBody =
                "{\n" +
                        "\t\"name\": \"Pedro\",\n" +
                        "\t\"email\": \"pedro@gmail.com\",\n" +
                        "\t\"birthDate\": \"09/02/2002\",\n" +
                        "\t\"password\": \"345868\"\n" +
                        "}";

        String jwt = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .extract().path("jwt");

        System.out.println(jwt);
    }

    @Test
    void shouldAuthenticateUser() {

        UserModel user = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe@gmail.com",
                "17/08/2001"
        );

        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);

        String requestBody =
                "{\n" +
                "\t\"email\": \"felipe@gmail.com\",\n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        String jwt = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract().path("jwt");

        System.out.println(jwt);
    }

    @Test
    void shouldGetOneUser() {

        UserModel user = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe@gmail.com",
                "17/08/2001"
        );

        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);

        String requestBody =
                "{\n" +
                "\t\"email\": \"felipe@gmail.com\",\n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        String jwt = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().path("jwt");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .body("felipe@gmail.com")
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldGetAllUsers() {
        UserModel user1 = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe1@gmail.com",
                "17/08/2001"
        );

        UserModel user2 = new UserModel(
                2L,
                "Bruno Silva",
                "bruno@gmail.com",
                "17/08/2001"
        );

        user1.setPassword(passwordEncoder.encode("123456"));
        user2.setPassword(passwordEncoder.encode("123456"));
        List<UserModel> users = List.of(
                user1, user2

        );
        userRepository.saveAll(users);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsIm5hbWUiOiJGZWxpcGUgRW56byIsInN1YiI6ImZlbGlwZTFAZ21haWwuY29tIiwiaWF0IjoxNzE4MjQxMjQzLCJleHAiOjE3MTgyNDMwNDN9.rQVqk-vNSnFuqsYwB1ECZIWwumIBp39jAG3Z0EpQ_S4"
                )
                .body("{" +
                        "\t\"email\": \"bruno@gmail.com\"" +
                        "}"
                )
                .when()
                .get("/user")
                .then()
                .statusCode(200)
                .body(".", hasSize(2));
    }

    @Test
    void shouldFailPostDuoToAge() {
        String requestBody =
                "{\n" +
                        "\t\"name\": \"Pedro\",\n" +
                        "\t\"email\": \"pedro@gmail.com\",\n" +
                        "\t\"birthDate\": \"09/02/2007\",\n" +
                        "\t\"password\": \"345868\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/user")
                .then()
                .statusCode(400)
                .body("message", equalTo("Error in Business Rules"));
    }

    @Test
    void shouldFailPostDuoToEmailAlreadyExists() {

        UserModel user = new UserModel(
                1L,
                "Junior Souza",
                "junior@gmail.com",
                "17/08/2001"
        );
        user.setPassword(passwordEncoder.encode("123456"));

        userRepository.save(user);
        String requestBody =
                "{\n" +
                        "\t\"name\": \"Junior Souza\",\n" +
                        "\t\"email\": \"junior@gmail.com\",\n" +
                        "\t\"birthDate\": \"17/08/2001\",\n" +
                        "\t\"password\": \"123456\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/user")
                .then()
                .statusCode(400)
                .body("message", equalTo("Error in Business Rules"));
    }

    @Test
    void shouldFailPostDuoToInvalidDataFormat() {
        String requestBody =
                "{\n" +
                        "\t\"name\": \"\",\n" +
                        "\t\"email\": \"junior@gmail.com\",\n" +
                        "\t\"birthDate\": \"17/08/2001\",\n" +
                        "\t\"password\": \"123456\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid data format"));
    }
}