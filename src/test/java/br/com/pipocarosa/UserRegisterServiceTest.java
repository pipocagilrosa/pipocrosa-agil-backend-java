package br.com.pipocarosa;

import br.com.pipocarosa.models.UserModel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import br.com.pipocarosa.models.enums.Role;
import br.com.pipocarosa.repositories.UserRepository;
import br.com.pipocarosa.services.UserRegisterService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.catalina.User;
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


    // UNIT TESTS
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
    void shouldVerifyIfEmailAlreadyExists() {
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
    void shouldVerifyIfEmailNotExists() {
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
    void shoulGetUserByUuid() {
        UserModel user = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe@gmail.com",
                "17/08/2001"
        );

        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);

        UUID uuid = user.getUuid();

        Optional<UserModel> userModel = userRepository.findByUuid(uuid);
        assertTrue(userModel.isPresent());
    }


    // INTEGRATION TESTS

    // Post - register
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
                .post("/register")
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
                .post("/register")
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
                .post("/register")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid data format"));
    }

    @Test
    void shouldFailedPostDueToEmailAlreadyExists() {

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
                        "\t\"name\": \"Junior Fernandes\",\n" +
                        "\t\"email\": \"junior@gmail.com\",\n" +
                        "\t\"birthDate\": \"09/02/2002\",\n" +
                        "\t\"password\": \"345868\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/register")
                .then()
                .statusCode(409);
    }


    // Post - login
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
    void shouldFailAuthenticationDueToInvalidDataFormat() {
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
                        "\t\"email\": \"felipegmail.com\",\n" +
                        "\t\"password\": \"123456\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid data format"));
    }

    @Test
    void shouldFailAuthenticationDueToUserNotFound() {
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
                        "\t\"email\": \"jose@gmail.com\",\n" +
                        "\t\"password\": \"123456\"\n" +
                        "}";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("User not found"));
    }

    // GetOne
    @Test
    void shouldGetOneUser() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe@gmail.com",
                "17/08/2001"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody =
                "{\n" +
                        "\t\"email\": \"felipe@gmail.com\",\n" +
                        "\t\"password\": \"123456\"\n" +
                        "}";

        // Create a test scenario given the request body by making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract and save jwt from the response body
        String jwt = response.path("jwt");
        // Extract and save uuid from the response body
        String uuid = response.path("uuid");

        String path = "/user/" + uuid;

        // Create a test scenario given the authorization header with the bearer token
        // by making a get with path /user/uuid and expecting a 200 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .get(path)
                .then()
                .statusCode(200);
    }

    @Test
    void shouldFailGetOneUserDueToInvalidUuidFormat() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/user/" + uuid + 1;

        // Create a test scenario given the authentication header, making a delete with path /user
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .get(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid uuid format"));

    }

    @Test
    void shouldFailGetOneUserDueToInvalidToken() {

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

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract and save the jwt from the response body
        String jwt = response.path("jwt" + 1);
        // Extract and save the uuid from the response body
        String uuid = response.path("uuid");

        String path = "/user/" + uuid;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .get(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid token"));
    }

    @Test
    void shouldFailGetOneUserDueToUserNotFound() {
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

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract and save the jwt from the response body
        String jwt = response.path("jwt");
        // Save a random uuid
        String uuid = "f14d7947-5f1b-463f-a365-109329a719bf";

        String path = "/user/" + uuid;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .get(path)
                .then()
                .statusCode(404)
                .body("message", equalTo("User not found"));
    }


    // GetAll
    @Test
    void shouldGetAllUsers() {
        UserModel user1 = new UserModel(
                1L,
                "Felipe Enzo",
                "felipe@gmail.com",
                "17/08/2001"
        );

        UserModel user2 = new UserModel(
                2L,
                "Bruno Silva",
                "bruno@gmail.com",
                "17/08/2001"
        );

        user1.setPassword(passwordEncoder.encode("123456"));
        user1.setRole(Role.ADMIN);
        user2.setPassword(passwordEncoder.encode("123456"));
        List<UserModel> users = List.of(
                user1, user2
        );
        userRepository.saveAll(users);

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
                .header("Authorization",
                        "Bearer " + jwt)
                .when()
                .get("/users")
                .then()
                .statusCode(200);
    }


    // Delete
    @Test
    void shouldDeleteUser() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/user/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database
        assertTrue(optionalUser1.isPresent());

        // Create a test scenario given the authentication header, making a delete with path /user
        // and check if it returns a 204 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .delete(path)
                .then()
                .statusCode(204);

        Optional<UserModel> optionalUser2 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is not present in the database anymore
        assertFalse(optionalUser2.isPresent());

    }

    @Test
    void shouldFailDeleteUserDueToInvalidUuidFormat() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/user/" + uuid + 1;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a test scenario given the authentication header, making a delete with path /user
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .delete(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid uuid format"));

        Optional<UserModel> optionalUser2 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is still present in the database
        assertTrue(optionalUser2.isPresent());

    }

    @Test
    void shouldFailDeleteUserDueToUserNotFound() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Save a random uuid
        String uuid = "f14d7947-5f1b-463f-a365-109329a719bf";

        String path = "/user/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a test scenario given the authentication header, making a delete with path /user
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .when()
                .delete(path)
                .then()
                .statusCode(404)
                .body("message", equalTo("User not found"));

        Optional<UserModel> optionalUser2 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is still present in the database
        assertTrue(optionalUser2.isPresent());

    }


    // Update - data
    @Test
    void shouldUpdateUserData() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/user/" + uuid;

        Optional<UserModel> optionalUser = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database
        assertTrue(optionalUser.isPresent());

        // Create a request body in json format with name and birthDate changes
        requestBody =
                "{\n" +
                        "\t\"name\": \"Pedro Melo\",\n" +
                        "\t\"birthDate\": \"09/02/2002\"\n" +
                        "}";

        // Create a test scenario given the authentication header, making a put with path /user
        // and check if it returns a 200 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + jwt)
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(200);

        // Gets the updated user from database
        optionalUser = userRepository.findByUuid(user.getUuid());
        UserModel userFound = optionalUser.get();

        // Validates if user's name and birthdate were updated
        assertEquals(userFound.getName(), "Pedro Melo");
        assertEquals(userFound.getBirthDate(), "09/02/2002");
    }

    @Test
    void shouldFailPutDueToInvalidUuidFormat() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/user/" + uuid + 1;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name and birthDate changes
        requestBody =
                "{\n" +
                        "\t\"name\": \"Pedro Melo\",\n" +
                        "\t\"birthDate\": \"09/02/2002\"\n" +
                        "}";

        // Create a test scenario given the authentication header, making a put with path /user
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid uuid format"));
    }

    @Test
    void shouldFailPutDueToUserNotFound() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Save a random uuid
        String uuid = "f14d7947-5f1b-463f-a365-109329a719bf";

        String path = "/user/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name and birthDate changes
        requestBody =
                "{\n" +
                        "\t\"name\": \"Pedro Melo\",\n" +
                        "\t\"birthDate\": \"09/02/2002\"\n" +
                        "}";

        // Create a test scenario given the authentication header, making a put with path /user
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(404)
                .body("message", equalTo("User not found"));
    }

    @Test
    void shouldFailPutDueToInvalidDataFormat() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Save a random uuid
        String uuid = response.path("uuid");

        String path = "/user/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name empty
        requestBody =
                "{\n" +
                        "\t\"name\": \"\",\n" +
                        "\t\"birthDate\": \"09/02/2002\"\n" +
                        "}";

        // Create a test scenario given the authentication header, making a put with path /user
        // and check if it returns a 400 code and negative message
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid data format"));
    }


    // Update - password

    @Test
    void shouldUpdatePassword() {

        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/password/" + uuid;

        Optional<UserModel> optionalUser = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database
        assertTrue(optionalUser.isPresent());

        // Create a request body in json format with password changes
        requestBody =
                "{\n" +
                "\t\"password\": \"654321\"\n" +
                "}";

        // Create a test scenario given the authentication header, making a put with path /user
        // and check if it returns a 200 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(200);

        // Gets the updated user from database
        optionalUser = userRepository.findByUuid(user.getUuid());
        UserModel userFound = optionalUser.get();

        // Validates if user's password was updated
        assertTrue(passwordEncoder.matches("654321", userFound.getPassword()));
    }

    @Test
    void shouldFailPutPasswordDueToInvalidUuidFormat() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Extract uuid from the response
        String uuid = response.path("uuid");

        String path = "/password/" + uuid + 1;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name and birthDate changes
        requestBody =
                "{\n" +
                        "\t\"password\": \"654321\"\n" +
                        "}";

        // Create a test scenario given the authentication header, making a put with path /password
        // and check if it returns a 400 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid uuid format"));
    }

    @Test
    void shouldFailPutPasswordDueToUserNotFound() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Save a random uuid
        String uuid = "f14d7947-5f1b-463f-a365-109329a719bf";

        String path = "/password/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name and birthDate changes
        requestBody =
                "{\n" +
                "\t\"password\": \"654321\"\n" +
                "}";

        // Create a test scenario given the authentication header, making a put with path /password
        // and check if it returns a 404 code
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(404)
                .body("message", equalTo("User not found"));
    }

    @Test
    void shouldFailPutPasswordDueToInvalidDataFormat() {
        // Create a new User
        UserModel user = new UserModel(
                1L,
                "Jose Alberto",
                "jose@gmail.com",
                "21/01/2000"
        );

        // Set the user password encoded
        user.setPassword(passwordEncoder.encode("123456"));

        // Save the user to the database
        userRepository.save(user);

        // Create a request body in json format with email and password
        String requestBody = "{\n" +
                "\t\"email\": \"jose@gmail.com\", \n" +
                "\t\"password\": \"123456\"\n" +
                "}";

        // Create a test scenario given the request body, making a post with
        // path /login and save the response body
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post("/login")
                .then()
                .extract().response();

        // Extract jwt from the response
        String jwt = response.path("jwt");

        // Save a random uuid
        String uuid = response.path("uuid");

        String path = "/password/" + uuid;

        Optional<UserModel> optionalUser1 = userRepository.findByUuid(user.getUuid());

        // Assert that the user is present in the database at first
        assertTrue(optionalUser1.isPresent());

        // Create a request body in json format with name empty
        requestBody =
                "{\n" +
                "\t\"password\": \"12345\"\n" +
                "}";

        // Create a test scenario given the authentication header, making a put with path /password
        // and check if it returns a 400 code and negative message
        RestAssured.given()
                .contentType(ContentType.JSON)
                .and()
                .header("Authorization", "Bearer " + jwt)
                .and()
                .body(requestBody)
                .when()
                .put(path)
                .then()
                .statusCode(400)
                .body("message", equalTo("Invalid data format"));
    }

}