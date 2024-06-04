package io.github.vinifillos.integrationTests.controller.cors.withJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vinifillos.configs.ConfigTest;
import io.github.vinifillos.integrationTests.dto.AccountCredentialsDto;
import io.github.vinifillos.integrationTests.dto.PersonDto;
import io.github.vinifillos.integrationTests.dto.TokenDto;
import io.github.vinifillos.integrationTests.testContainers.AbstractIntegrationTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PersonControllerCorsJsonTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;
    private static PersonDto personDto;

    @BeforeAll
    static void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        personDto = new PersonDto();
    }

    @Test
    @Order(0)
    void CreateAccessToken() {
        AccountCredentialsDto user = new AccountCredentialsDto("leandro", "admin123");
        var accessToken = given()
                .basePath("/auth/signin")
                .port(ConfigTest.SERVER_PORT)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .body(user)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenDto.class)
                .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(ConfigTest.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/person/v1")
                .setPort(ConfigTest.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    void CreatePerson_WithValidCORS_ReturnsPerson() throws JsonProcessingException {
        mockPerson();

        var content = given()
                .spec(specification)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .body(personDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonDto persistedPerson = objectMapper.readValue(content, PersonDto.class);
        personDto = persistedPerson;

        assertNotNull(persistedPerson);
        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getGender());
        assertNotNull(persistedPerson.getAddress());
        assertNotNull(persistedPerson.getEnabled());

        assertTrue(persistedPerson.getId() > 0);

        assertEquals("Richard", persistedPerson.getFirstName());
        assertEquals("Stallman", persistedPerson.getLastName());
        assertEquals("Male", persistedPerson.getGender());
        assertEquals("New York City, New York, US", persistedPerson.getAddress());
        assertTrue(persistedPerson.getEnabled());
    }

    @Test
    @Order(2)
    void UpdatePerson_WithValidCORS_ReturnsPerson() throws JsonProcessingException {
        personDto.setLastName("Modified Last Name");

        var content = given()
                .spec(specification)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .body(personDto)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonDto persistedPerson = objectMapper.readValue(content, PersonDto.class);
        personDto = persistedPerson;

        assertNotNull(persistedPerson);
        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getGender());
        assertNotNull(persistedPerson.getAddress());
        assertNotNull(persistedPerson.getEnabled());

        assertEquals(personDto.getId(), persistedPerson.getId());

        assertEquals("Richard", persistedPerson.getFirstName());
        assertEquals("Modified Last Name", persistedPerson.getLastName());
        assertEquals("Male", persistedPerson.getGender());
        assertEquals("New York City, New York, US", persistedPerson.getAddress());
        assertTrue(persistedPerson.getEnabled());
    }

    @Test
    @Order(3)
    void FindPerson_WithValidCORS_ReturnsPerson() throws JsonProcessingException {
        mockPerson();

        var content = given()
                .spec(specification)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .header(ConfigTest.HEADER_PARAM_ORIGIN, ConfigTest.ORIGIN_VINIFILLOS)
                .pathParam("id", personDto.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        PersonDto persistedPerson = objectMapper.readValue(content, PersonDto.class);
        personDto = persistedPerson;

        assertNotNull(persistedPerson);
        assertNotNull(persistedPerson.getId());
        assertNotNull(persistedPerson.getFirstName());
        assertNotNull(persistedPerson.getLastName());
        assertNotNull(persistedPerson.getGender());
        assertNotNull(persistedPerson.getAddress());
        assertNotNull(persistedPerson.getEnabled());

        assertTrue(persistedPerson.getId() > 0);

        assertEquals("Richard", persistedPerson.getFirstName());
        assertEquals("Modified Last Name", persistedPerson.getLastName());
        assertEquals("Male", persistedPerson.getGender());
        assertEquals("New York City, New York, US", persistedPerson.getAddress());
        assertTrue(persistedPerson.getEnabled());
    }

    @Test
    @Order(4)
    void DeletePerson_WithValidCORS_ReturnsNothing() {
        given()
                .spec(specification)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .pathParam("id", personDto.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void testFindAll_WithValidToken_ReturnsList() throws JsonProcessingException {

        var content = given().spec(specification)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        List<PersonDto> people = objectMapper.readValue(content, new TypeReference<>() {});

        PersonDto personOne = people.getFirst();

        assertNotNull(personOne.getId());
        assertNotNull(personOne.getFirstName());
        assertNotNull(personOne.getLastName());
        assertNotNull(personOne.getAddress());
        assertNotNull(personOne.getGender());
        assertNotNull(personOne.getEnabled());

        assertEquals(1, personOne.getId());

        assertEquals("Vinicius", personOne.getFirstName());
        assertEquals("Fillos", personOne.getLastName());
        assertEquals("Street Alfredo Kamisnki", personOne.getAddress());
        assertEquals("Male", personOne.getGender());
        assertTrue(personOne.getEnabled());

        PersonDto personTwo = people.get(1);

        assertNotNull(personTwo.getId());
        assertNotNull(personTwo.getFirstName());
        assertNotNull(personTwo.getLastName());
        assertNotNull(personTwo.getAddress());
        assertNotNull(personTwo.getGender());
        assertNotNull(personTwo.getEnabled());

        assertEquals(2, personTwo.getId());

        assertEquals("Pietra", personTwo.getFirstName());
        assertEquals("Canesso", personTwo.getLastName());
        assertEquals("Street street", personTwo.getAddress());
        assertEquals("Female", personTwo.getGender());
        assertTrue(personTwo.getEnabled());
    }

    @Test
    @Order(6)
    void testFindAll_WithoutToken_ReturnsException() {

        RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/person/v1")
                .setPort(ConfigTest.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given().spec(specificationWithoutToken)
                .contentType(ConfigTest.CONTENT_TYPE_JSON)
                .when()
                .get()
                .then()
                .statusCode(403);
    }


    private void mockPerson() {
        personDto.setFirstName("Richard");
        personDto.setLastName("Stallman");
        personDto.setAddress("New York City, New York, US");
        personDto.setGender("Male");
        personDto.setEnabled(true);
    }
}