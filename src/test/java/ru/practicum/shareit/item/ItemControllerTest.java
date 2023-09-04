package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    private ItemDto itemDto = ItemDto.builder()
            .name("Test Item")
            .description("Test Description")
            .available(true)
            .build();


    @Test
    public void testCreateItem() throws Exception {

        long userId = 123;

        ItemDto expectedItemDto = ItemDto.builder()
                .id(1L).name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();

        when(itemService.create(itemDto, userId)).thenReturn(expectedItemDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(expectedItemDto.getId()))
                .andExpect(jsonPath("$.name").value(expectedItemDto.getName()))
                .andExpect(jsonPath("$.description").value(expectedItemDto.getDescription()));

        verify(itemService, times(1)).create(itemDto, userId);
    }

    @Test
    public void testCreateItemWithInvalidDto() throws Exception {

        long userId = 123;

        ItemDto blankFieldsDto = ItemDto.builder()
                .name("")
                .description("")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(blankFieldsDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(3)))
                .andExpect(jsonPath("$.['Bad Request']",
                        containsInAnyOrder("Необходимо указать доступность для аренды",
                                "Необходимо указать название",
                                "Необходимо указать описание")));


        verify(itemService, times(0)).create(any(), anyLong());
    }

    @Test
    public void testUpdateItem() throws Exception {
        // Arrange
        long itemId = 1L;
        long userId = 123L;

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();


        ItemDto expectedItemDto = ItemDto.builder()
                .id(1L)
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemService.update(itemId, userId, updateDto))
                .thenReturn(expectedItemDto);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(expectedItemDto.getId()))
                .andExpect(jsonPath("$.name").value(expectedItemDto.getName()))
                .andExpect(jsonPath("$.description").value(expectedItemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(expectedItemDto.getAvailable()));

        verify(itemService, times(1)).update(itemId, userId, updateDto);
    }

    @Test
    public void testGetItemById() throws Exception {
        // Arrange
        long itemId = 1L;
        long userId = 123;

        ItemDto expectedItemDto = ItemDto.builder()
                .id(1L).name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();

        when(itemService.getById(itemId, userId)).thenReturn(expectedItemDto);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(expectedItemDto.getId()))
                .andExpect(jsonPath("$.name").value(expectedItemDto.getName()))
                .andExpect(jsonPath("$.description").value(expectedItemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(expectedItemDto.getAvailable()));

        verify(itemService, times(1)).getById(itemId, userId);
    }

    @Test
    public void testGetAllItemsForUser() throws Exception {
        // Arrange
        long userId = 123;
        int from = 0;
        int size = 10;

        List<ItemDto> expectedItems = new ArrayList<>();
        expectedItems.add(ItemDto.builder().id(1L).name("Test Item 1").description("Test Description 1").build());
        expectedItems.add(ItemDto.builder().id(2L).name("Test Item 2").description("Test Description 2").build());


        when(itemService.getAllForUser(userId,
                PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(expectedItems);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$[0].id").value(expectedItems.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(expectedItems.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(expectedItems.get(0).getDescription()))
                .andExpect(jsonPath("$[1].id").value(expectedItems.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(expectedItems.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(expectedItems.get(1).getDescription()));

        verify(itemService, times(1)).getAllForUser(userId,
                PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created")));
    }

    @Test
    public void testGetAllItemsForUserWithInvalidPaging() throws Exception {
        // Arrange
        long userId = 123;
        int from = -1;
        int size = 0;

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForUser.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForUser.size: must be greater than 0")));

        verify(itemService, times(0))
                .getAllForUser(anyLong(), any(Pageable.class));
    }

    @Test
    public void testSearchItemsByNameAndDescr() throws Exception {
        // Arrange
        String text = "test";
        long userId = 123;
        int from = 0;
        int size = 10;

        List<ItemDto> expectedItems = new ArrayList<>();
        expectedItems.add(ItemDto.builder()
                .id(1L)
                .name("Test Item 1")
                .description("Test Description 1")
                .build());
        expectedItems.add(ItemDto.builder()
                .id(2L).name("Test Item 2")
                .description("Test Description 2")
                .build());

        when(itemService.searchByNameAndDescr(text, userId, PageRequest.of(
                from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(expectedItems);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$[0].id").value(expectedItems.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(expectedItems.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(expectedItems.get(0).getDescription()))
                .andExpect(jsonPath("$[1].id").value(expectedItems.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(expectedItems.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(expectedItems.get(1).getDescription()));

        verify(itemService, times(1))
                .searchByNameAndDescr(text, userId, PageRequest.of(
                        from / size, size, Sort.by(Sort.Direction.DESC, "created")));
    }

    @Test
    public void testCreateComment() throws Exception {
        // Arrange
        CommentDto commentDto = CommentDto.builder()
                .text("Test Comment")
                .build();

        long itemId = 1L;
        long userId = 123L;

        CommentDto expectedCommentDto = CommentDto.builder()
                .id(1L)
                .text(commentDto.getText())
                .build();

        when(itemService.create(any(CommentDto.class), eq(itemId), eq(userId)))
                .thenReturn(expectedCommentDto);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(expectedCommentDto.getId()))
                .andExpect(jsonPath("$.text").value(expectedCommentDto.getText()));

        verify(itemService, times(1))
                .create(any(CommentDto.class), anyLong(), anyLong());
    }

    @Test
    public void testCreateCommentWithBlankText() throws Exception {
        // Arrange
        CommentDto commentDto = CommentDto.builder()
                .text("")
                .build();

        long itemId = 1L;
        long userId = 123L;

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']").value("must not be blank"));

        verify(itemService, times(0))
                .create(any(CommentDto.class), anyLong(), anyLong());
    }
}