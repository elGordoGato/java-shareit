package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    private final ItemRequestDto requestDto = ItemRequestDto.builder()
            .id(1L)
            .description("Test Description")
            .created(LocalDateTime.of(1975, 12, 13, 13, 44))
            .items(Collections.emptyList())
            .build();
    @MockBean
    private ItemRequestService requestService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreate() throws Exception {
        when(requestService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(requestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Description\"}"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                        .value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description")
                        .value("Test Description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created")
                        .value("1975-12-13@13:44:00.000347"))
                .andDo(print());
    }

    @Test
    public void testCreateNotFound() throws Exception {
        when(requestService.create(anyLong(), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("not found test"));

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test Description\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Not Found']")
                        .value("not found test"))
                .andDo(print());
    }

    @Test
    public void testCreateBlankDescription() throws Exception {
        when(requestService.create(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(requestDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("must not be blank"))
                .andDo(print());
    }


    @Test
    public void testGetOwn() throws Exception {

        List<ItemRequestDto> requestList = Collections.singletonList(requestDto);

        when(requestService.findAllByUserId(anyLong()))
                .thenReturn(requestList);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Test Description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created").value("1975-12-13@13:44:00.000347"))
                .andDo(print());
    }

    @Test
    public void testGetAll() throws Exception {

        List<ItemRequestDto> requestList = Arrays.asList(requestDto);

        when(requestService.findAll(anyLong(), any()))
                .thenReturn(requestList);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Test Description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].created").value("1975-12-13@13:44:00.000347"))
                .andDo(print());
    }


    @Test
    public void testGetAllNegativeFrom() throws Exception {

        List<ItemRequestDto> requestList = Collections.singletonList(requestDto);

        when(requestService.findAll(anyLong(), any()))
                .thenReturn(requestList);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("getAll.from: must be greater than or equal to 0"))
                .andDo(print());
    }

    @Test
    public void testGetAllZeroSize() throws Exception {

        List<ItemRequestDto> requestList = Collections.singletonList(requestDto);

        when(requestService.findAll(anyLong(), any()))
                .thenReturn(requestList);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "5")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("getAll.size: must be greater than 0"))
                .andDo(print());
    }

    @Test
    public void testGetById() throws Exception {

        when(requestService.findById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                        .value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description")
                        .value("Test Description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created")
                        .value("1975-12-13@13:44:00.000347"))
                .andDo(print());
    }
}