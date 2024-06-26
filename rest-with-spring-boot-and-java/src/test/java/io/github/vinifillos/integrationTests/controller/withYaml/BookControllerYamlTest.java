package io.github.vinifillos.integrationTests.controller.withYaml;

import io.github.vinifillos.configs.ConfigTest;
import io.github.vinifillos.integrationTests.controller.withYaml.mapper.YMLMapper;
import io.github.vinifillos.integrationTests.dto.AccountCredentialsDto;
import io.github.vinifillos.integrationTests.dto.BookDto;
import io.github.vinifillos.integrationTests.dto.TokenDto;
import io.github.vinifillos.integrationTests.dto.pagedModels.PagedModelBook;
import io.github.vinifillos.integrationTests.testContainers.AbstractIntegrationTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;
    private static YMLMapper objectMapper;
    private static BookDto bookDto;

    @BeforeAll
    public static void setup() {
        objectMapper = new YMLMapper();
        bookDto = new BookDto();
    }

    @Test
    @Order(0)
    void authorization() {
        AccountCredentialsDto user = new AccountCredentialsDto("leandro", "admin123");

        var accessToken = given()
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .basePath("/auth/signin")
                .port(ConfigTest.SERVER_PORT)
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .body(user, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(TokenDto.class, objectMapper)
                .getAccessToken();

        specification = new RequestSpecBuilder()
                .addHeader(ConfigTest.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken)
                .setBasePath("/api/book/v1")
                .setPort(ConfigTest.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    void testCreate() {
        mockBook();

        var persistedBookDto = given().spec(specification)
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .body(bookDto, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookDto.class, objectMapper);

        bookDto = persistedBookDto;

        assertNotNull(persistedBookDto);

        assertNotNull(persistedBookDto);
        assertNotNull(persistedBookDto.getId());
        assertNotNull(persistedBookDto.getAuthor());
        assertNotNull(persistedBookDto.getTitle());
        assertNotNull(persistedBookDto.getPrice());
        assertNotNull(persistedBookDto.getLaunchDate());

        assertTrue(persistedBookDto.getId() > 0);

        assertEquals("Robert C. Martin", persistedBookDto.getAuthor());
        assertEquals("Clean Code", persistedBookDto.getTitle());
        assertEquals(199.9D, persistedBookDto.getPrice());
    }

    @Test
    @Order(2)
    void testFindById() {
        mockBook();

        var persistedBookDto = given().spec(specification)
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .pathParam("id", bookDto.getId())
                .when()
                .get("{id}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookDto.class, objectMapper);

        bookDto = persistedBookDto;

        assertNotNull(persistedBookDto);

        assertNotNull(persistedBookDto);
        assertNotNull(persistedBookDto.getId());
        assertNotNull(persistedBookDto.getAuthor());
        assertNotNull(persistedBookDto.getTitle());
        assertNotNull(persistedBookDto.getPrice());
        assertNotNull(persistedBookDto.getLaunchDate());

        assertTrue(persistedBookDto.getId() > 0);

        assertEquals("Robert C. Martin", persistedBookDto.getAuthor());
        assertEquals("Clean Code", persistedBookDto.getTitle());
        assertEquals(199.9D, persistedBookDto.getPrice());
    }

    @Test
    @Order(3)
    void testUpdate() {
        bookDto.setTitle("Modified Title");

        var persistedBookDto = given().spec(specification)
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .body(bookDto, objectMapper)
                .when()
                .post()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(BookDto.class, objectMapper);

        bookDto = persistedBookDto;

        assertNotNull(persistedBookDto);

        assertNotNull(persistedBookDto);
        assertNotNull(persistedBookDto.getId());
        assertNotNull(persistedBookDto.getAuthor());
        assertNotNull(persistedBookDto.getTitle());
        assertNotNull(persistedBookDto.getPrice());
        assertNotNull(persistedBookDto.getLaunchDate());

        assertTrue(persistedBookDto.getId() > 0);

        assertEquals("Robert C. Martin", persistedBookDto.getAuthor());
        assertEquals("Modified Title", persistedBookDto.getTitle());
        assertEquals(199.9D, persistedBookDto.getPrice());
    }

    @Test
    @Order(4)
    void testDelete() {

        given().spec(specification)
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .pathParam("id", bookDto.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void testFindAll() {
        var wrapper = given().spec(specification)
                .config( RestAssuredConfig.config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(ConfigTest.CONTENT_TYPE_YML, ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .queryParams("page", 0, "size", 7, "direction", "asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(PagedModelBook.class, objectMapper);

        var books = wrapper.getContent();

        BookDto bookOne = books.getFirst();

        assertNotNull(bookOne);
        assertNotNull(bookOne.getId());
        assertNotNull(bookOne.getAuthor());
        assertNotNull(bookOne.getTitle());
        assertNotNull(bookOne.getPrice());
        assertNotNull(bookOne.getLaunchDate());

        assertEquals(12, bookOne.getId());

        assertEquals("Big Data: como extrair volume, variedade, velocidade e valor da avalanche de informação cotidiana", bookOne.getTitle());
        assertEquals("Viktor Mayer-Schonberger e Kenneth Kukier", bookOne.getAuthor());
        assertEquals(54.0D, bookOne.getPrice());

        BookDto bookNine = books.get(4);

        assertNotNull(bookNine);
        assertNotNull(bookNine.getId());
        assertNotNull(bookNine.getAuthor());
        assertNotNull(bookNine.getTitle());
        assertNotNull(bookNine.getPrice());
        assertNotNull(bookNine.getLaunchDate());

        assertEquals(8, bookNine.getId());

        assertEquals("Domain Driven Design", bookNine.getTitle());
        assertEquals("Eric Evans", bookNine.getAuthor());
        assertEquals(92.0D, bookNine.getPrice());
    }

    @Test
    @Order(6)
    void testFindAllWithoutToken() {
        RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
                .setBasePath("/api/book/v1")
                .setPort(ConfigTest.SERVER_PORT)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();

        given().spec(specificationWithoutToken)
                .config(
                        RestAssuredConfig
                                .config()
                                .encoderConfig(EncoderConfig.encoderConfig()
                                        .encodeContentTypeAs(
                                                ConfigTest.CONTENT_TYPE_YML,
                                                ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .when()
                .get()
                .then()
                .statusCode(403);
    }

    @Test
    @Order(7)
    void testHATEOAS() {

        var content = given().spec(specification)
                .config(RestAssuredConfig.config()
                        .encoderConfig(EncoderConfig.encoderConfig()
                                .encodeContentTypeAs(ConfigTest.CONTENT_TYPE_YML, ContentType.TEXT)))
                .contentType(ConfigTest.CONTENT_TYPE_YML)
                .accept(ConfigTest.CONTENT_TYPE_YML)
                .queryParams("page", 0, "size", 3, "direction", "desc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertTrue(content.contains("rel: \"self\"\n    href: \"http://localhost:8888/api/book/v1/1\""));
        assertTrue(content.contains("rel: \"self\"\n    href: \"http://localhost:8888/api/book/v1/6\""));
        assertTrue(content.contains("- rel: \"self\"\n    href: \"http://localhost:8888/api/book/v1/14\""));

        assertTrue(content.contains("page:\n  size: 3\n  totalElements: 15\n  totalPages: 5\n  number: 0"));

        assertTrue(content.contains("rel: \"first\"\n  href: \"http://localhost:8888/api/book/v1?direction=asc&page=0&size=3&sort=title,desc\""));
        assertTrue(content.contains("rel: \"self\"\n  href: \"http://localhost:8888/api/book/v1?page=0&size=3&direction=asc\""));
        assertTrue(content.contains("rel: \"next\"\n  href: \"http://localhost:8888/api/book/v1?direction=asc&page=1&size=3&sort=title,desc\""));
        assertTrue(content.contains("rel: \"last\"\n  href: \"http://localhost:8888/api/book/v1?direction=asc&page=4&size=3&sort=title,desc\""));
    }

    private void mockBook() {
        bookDto.setTitle("Clean Code");
        bookDto.setAuthor("Robert C. Martin");
        bookDto.setPrice(199.9D);
        bookDto.setLaunchDate(new Date());
    }
}