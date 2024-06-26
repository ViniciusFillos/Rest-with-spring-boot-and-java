package io.github.vinifillos.unitTests.services;

import io.github.vinifillos.exceptions.RequiredObjectIsNullException;
import io.github.vinifillos.model.Person;
import io.github.vinifillos.model.dto.PersonDto;
import io.github.vinifillos.repositories.PersonRepository;
import io.github.vinifillos.services.PersonService;
import io.github.vinifillos.mocks.MockPerson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    MockPerson input;

    @InjectMocks
    private PersonService personService;

    @Mock
    PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        input = new MockPerson();
    }

    @Test
    void findById_WithValidData_ReturnsPerson() {
        Person person = input.mockEntity(1);
        person.setId(1L);
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        var result = personService.findById(1L);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
    }

    @Test
    void create_WithValidData_ReturnPerson() {
        Person persisted = input.mockEntity(1);
        persisted.setId(1L);
        PersonDto dto = input.mockDto(1);
        dto.setKey(1L);
        when(personRepository.save(any(Person.class))).thenReturn(persisted);

        var result = personService.create(dto);
        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
    }

    @Test
    void create_WithNullPerson_ReturnException() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> personService.create(null));
        String expectedMessage = "It's not allowed to persist a null object!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void update_WithValidData_ReturnPerson() {
        Person entity = input.mockEntity(1);
        entity.setId(1L);
        PersonDto dto = input.mockDto(1);
        dto.setKey(1L);
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(entity));
        when(personRepository.save(entity)).thenReturn(entity);

        var result = personService.update(dto);

        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/person/v1/1>;rel=\"self\"]"));
        assertEquals("Addres Test1", result.getAddress());
        assertEquals("First Name Test1", result.getFirstName());
        assertEquals("Last Name Test1", result.getLastName());
        assertEquals("Female", result.getGender());
    }

    @Test
    void update_WithNullPerson_ReturnException() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> personService.update(null));
        String expectedMessage = "It's not allowed to persist a null object!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void delete_WithValidData_DoNotReturnException() {
        Person entity = input.mockEntity(1);
        entity.setId(1L);
        when(personRepository.findById(anyLong())).thenReturn(Optional.of(entity));

        assertDoesNotThrow(() -> personService.delete(1L));
    }
}