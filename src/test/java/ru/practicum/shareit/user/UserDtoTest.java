package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    public void testSerialize() throws Exception {

        UserDto userDto = new UserDto(null, "Duke", "duke@example.com");

        JsonContent<UserDto> result = this.json.write(userDto);

        assertThat(result).hasJsonPathStringValue("$.name");
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("Duke");
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isEqualTo("duke@example.com");
        assertThat(result).hasJsonPath("$.id");
    }

    @Test
    public void testDeserialize() throws Exception {

        String jsonContent = "{\"id\":\"42\", \"name\": \"Duke\"," +
                " \"email\":\"duke@d1k.net\"}";

        UserDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo("Duke");
        assertThat(result.getEmail()).isEqualTo("duke@d1k.net");
        assertThat(result.getId()).isEqualTo(42L);
    }


}