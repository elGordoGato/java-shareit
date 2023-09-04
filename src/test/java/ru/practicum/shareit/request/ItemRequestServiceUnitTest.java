package ru.practicum.shareit.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;


    @Test
    void create_ValidRequest_ReturnsCreatedRequest() {
        // Arrange
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("request example")
                .build();
        User user = getUser(1);

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(requestRepository.save(ArgumentMatchers.any(ItemRequest.class)))
                .thenReturn(new ItemRequest(1L, requestDto.getDescription(), user));

        // Act
        ItemRequestDto resultDto = itemRequestService.create(userId, requestDto);

        // Assert
        assertThat(resultDto, notNullValue());
        assertThat(resultDto.getId(), equalTo(1L));
        assertThat(resultDto.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(resultDto.getCreated(), instanceOf(LocalDateTime.class));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1))
                .save(ArgumentMatchers.any(ItemRequest.class));
    }

    @Test
    void create_InvalidUserId_ThrowsNotFoundException() {
        // Arrange
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("request example").build();
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            itemRequestService.create(userId, requestDto);
        });
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(0))
                .save(ArgumentMatchers.any(ItemRequest.class));
    }

    @Test
    void findByUserId_ValidUserId_ReturnsRequestList() {
        // Arrange
        long userId = 1L;
        User user = getUser(1);
        ItemRequest itemRequest = getItemRequest(1, user);
        List<ItemRequest> requestList = List.of(itemRequest);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(requestRepository.findAllByAuthorId(anyLong()))
                .thenReturn(requestList);
        when(itemRepository.findAllByRequestIdIn(any()))
                .thenReturn(List.of(new Item(
                        1L, "item", "descr", true, user, itemRequest)));

        // Act
        List<ItemRequestDto> result = itemRequestService.findAllByUserId(1L);

        // Assert
        Assertions.assertNotNull(result);
        assertThat(result, hasSize(1));
        assertThat(result.get(0), hasProperty("id", equalTo(1L)));
        assertThat(result.get(0), hasProperty("description", equalTo("request 1 descr")));
        assertThat(result.get(0), hasProperty("created", instanceOf(LocalDateTime.class)));
        assertThat(result.get(0), hasProperty("items", hasSize(1)));
        assertThat(result.get(0).getItems().get(0), hasProperty("name", equalTo("item")));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).findAllByAuthorId(anyLong());
        verify(itemRepository, times(1)).findAllByRequestIdIn(any());
    }

    @Test
    void findByUserId_InvalidUserId_ThrowsNotFoundException() {
        // Arrange
        long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.findAllByUserId(userId);
        });
        assertThat(exception.getMessage(),
                equalTo("User with id: " + userId +
                        " requesting to get all own requests was not found"));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(0)).findAllByAuthorId(anyLong());
        verify(itemRepository, times(0)).findAllByRequestIdIn(any());

    }


    @Test
    public void testFindAll_WithValidUserId_ShouldReturnItemRequestDtos() {
        long userId = 1L;
        Pageable pageRequest = mock(Pageable.class);

        User requestingUser = getUser(1);
        User requestAuthor = getUser(2);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(requestingUser));

        ItemRequest itemRequest1 = getItemRequest(1, requestAuthor);
        ItemRequest itemRequest2 = getItemRequest(2, requestAuthor);
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);
        when(requestRepository.findAllByAuthorIdNot(userId, pageRequest))
                .thenReturn(itemRequests);

        Item item1 = getItem(1, requestingUser, itemRequest1);
        when(itemRepository.findAllByRequestIdIn(any()))
                .thenReturn(List.of(item1));

        List<ItemRequestDto> expectedDtos = new ArrayList<>();
        ItemRequestDto dto1 = getItemRequestDto(itemRequest1, List.of(item1));
        ItemRequestDto dto2 = getItemRequestDto(itemRequest2, Collections.emptyList());
        expectedDtos.add(dto1);
        expectedDtos.add(dto2);

        List<ItemRequestDto> result = itemRequestService.findAll(userId, pageRequest);

        assertThat(result, equalTo(expectedDtos));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).findAllByAuthorIdNot(anyLong(), any());
        verify(itemRepository, times(1)).findAllByRequestIdIn(any());
    }

    @Test
    public void testFindAll_WithInvalidUserId_ShouldThrowException() {
        long userId = 1L;
        Pageable pageRequest = mock(Pageable.class);
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.findAll(userId, pageRequest));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(0)).findAllByAuthorIdNot(anyLong(), any());
        verify(itemRepository, times(0)).findAllByRequestIdIn(any());
    }

    @Test
    public void testFindById() {
        // Create a mock user
        User user = getUser(1);

        // Create a mock item request
        ItemRequest itemRequest = getItemRequest(1, user);

        // Create a mock item
        Item item1 = getItem(1, getUser(2), itemRequest);
        Item item2 = getItem(2, getUser(2), itemRequest);

        // Mock the repository methods
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(Mockito.anySet()))
                .thenReturn(List.of(item1, item2));

        // Call the method
        ItemRequestDto result = itemRequestService.findById(1L, 1L);
        ItemRequestDto expectedDto = getItemRequestDto(itemRequest, List.of(item1, item2));

        // Assert the result
        assertThat(result, notNullValue());
        assertThat(result, equalTo(expectedDto));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestIdIn(any());
    }

    @Test()
    public void testFindByIdUserNotFound() {
        long userId = 1L;
        long requestId = 1L;
        // Mock the repository methods
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        // Call the method

        Exception exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(userId, requestId));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(0)).findById(anyLong());
        verify(itemRepository, times(0)).findAllByRequestIdIn(any());
        assertThat(exception.getMessage(), equalTo(String.format(
                "User with id: %s requesting to get request with id: %s was not found", userId, requestId)));
        // The test should throw a NotFoundException
    }

    @Test()
    public void testFindByIdRequestNotFound() {
        long requestId = 1L;
        User user = getUser(1);
        // Mock the repository methods
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        // Call the method

        Exception exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.findById(user.getId(), requestId));
        verify(userRepository, times(1)).findById(anyLong());
        verify(requestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(0)).findAllByRequestIdIn(any());
        assertThat(exception.getMessage(), equalTo(
                "Item request with id: " + requestId + " was not found"));
        // The test should throw a NotFoundException
    }

    private User getUser(int userNumber) {
        switch (userNumber) {
            case 1:
                return new User(1L, "John", "john@doe.com");
            case 2:
                return new User(2L, "Bob", "bob@example.com");
            default:
                return new User();
        }
    }

    private ItemRequest getItemRequest(int requestNumber, User requestAuthor) {
        switch (requestNumber) {
            case 1:
                return new ItemRequest(1L, "request 1 descr", requestAuthor);
            case 2:
                return new ItemRequest(2L, "request 2 descr", requestAuthor);
            default:
                return new ItemRequest();
        }
    }

    private Item getItem(int itemNumber, User owner, ItemRequest request) {
        switch (itemNumber) {
            case 1:
                return new Item(1L, "item No1", "descr for No1", true, owner, request);
            case 2:
                return new Item(2L, "item No2", "descr for No2", true, owner, request);
            default:
                return new Item();
        }
    }

    private ItemRequestDto getItemRequestDto(ItemRequest request, List<Item> items) {
        return ItemRequestDto.builder().id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream()
                        .map(item -> ItemDto.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .description(item.getDescription())
                                .available(item.getAvailable())
                                .comments(Collections.emptyList())
                                .requestId(request.getId())
                                .build()).collect(Collectors.toList()))
                .build();
    }
}