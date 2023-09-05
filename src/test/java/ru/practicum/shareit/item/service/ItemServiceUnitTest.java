package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShort;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.status.Status.APPROVED;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {
    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    public void testCreate() {
        // Create a sample ItemDto object
        ItemDto itemDto = ItemDto.builder()
                .name("item No1")
                .description("descr for No1")
                .available(true)
                .build();
        User user = getUser(1);
        Item item = getItem(1, user, null);

        // Set up any necessary dependencies or mocks
        when(userService.findById(anyLong()))
                .thenReturn(user);
        when(itemRepository.save(any(Item.class)))
                .thenReturn(item);

        // Call the create method
        ItemDto result = itemService.create(itemDto, 1L);

        // Assert the result
        assertThat(result, notNullValue());
        assertThat(itemDto.getName(), equalTo(result.getName()));
        assertThat(itemDto.getDescription(), equalTo(result.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(result.getAvailable()));
        assertThat(Collections.emptyList(), equalTo(result.getComments()));
        assertThat(itemDto.getLastBooking(), equalTo(result.getLastBooking()));
        assertThat(itemDto.getNextBooking(), equalTo(result.getNextBooking()));
        assertThat(itemDto.getRentCounter(), equalTo(result.getRentCounter()));
        assertThat(itemDto.getRequestId(), equalTo(result.getRequestId()));
        verify(userService, times(1)).findById(anyLong());
        verify(requestRepository, times(0)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
        // Add more assertions as needed
    }

    @Test
    public void testCreateUserNotFound() {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("item No1")
                .description("descr for No1")
                .available(true)
                .build();

        // Set up any necessary dependencies or mocks
        when(userService.findById(anyLong()))
                .thenThrow(new NotFoundException(String.format(
                        "User with id %s not found when trying to create item: %s", userId, itemDto)));

        // Call the create method
        Exception exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, userId));

        // Assert the result
        assertThat(exception.getMessage(), equalTo(
                String.format("User with id %s not found when trying to create item: %s", userId, itemDto)));
        verify(userService, times(1)).findById(anyLong());
        verify(requestRepository, times(0)).findById(anyLong());
        verify(itemRepository, times(0)).save(any(Item.class));

    }

    @Test
    public void testUpdate() {
        // Mock necessary dependencies and setup test data
        Long itemId = 1L;
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();
        User owner = getUser(1);
        User otherUser = getUser(2);
        ItemRequest request = getItemRequest(1, otherUser);

        Item targetItem = getItem(1, owner, request);
        List<BookingsByItem> bookings = getBookings(targetItem, otherUser);
        List<Comment> comments = getCommentsForItem(targetItem, otherUser);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(targetItem));
        when(itemRepository.save(any(Item.class)))
                .then(returnsFirstArg());
        when(bookingRepository.findDatesByItemId(eq(List.of(itemId)), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(comments);

        // Call the update method
        ItemDto updatedItemDto = itemService.update(itemId, userId, itemDto);

        // Assertions
        assertThat(updatedItemDto, notNullValue());
        assertThat(updatedItemDto.getName(), equalTo(itemDto.getName()));
        assertThat(updatedItemDto.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(updatedItemDto.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(updatedItemDto.getRequestId(), equalTo(request.getId()));
        assertThat(updatedItemDto.getLastBooking(), equalTo(getBookingShort(bookings.get(0).getLastBooking())));
        assertThat(updatedItemDto.getNextBooking(), equalTo(getBookingShort(bookings.get(0).getNextBooking())));
        assertThat(updatedItemDto.getRentCounter(), equalTo(5L));
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .save(any());
        verify(bookingRepository, times(1))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(1))
                .findAllByItemIdIn(anyList());
    }

    @Test
    public void testUpdateByNotOwner() {
        // Mock necessary dependencies and setup test data
        Long itemId = 1L;
        long userId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();
        User owner = getUser(1);
        User otherUser = getUser(2);
        ItemRequest request = getItemRequest(1, otherUser);

        Item targetItem = getItem(1, owner, request);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(targetItem));

        // Call the update method
        Exception exception = assertThrows(ForbiddenException.class,
                () -> itemService.update(itemId, userId, itemDto));

        // Assertions
        assertThat(exception.getMessage(), equalTo(
                String.format("У пользователя %s нет прав редактирровать товар %s", userId, itemId)));
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(0))
                .save(any());
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(0))
                .findAllByItemIdIn(anyList());
    }


    @Test
    public void testUpdateItemNotFound() {
        // Mock necessary dependencies and setup test data
        Long itemId = 2L;
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        // Call the update method
        Exception exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemId, userId, itemDto));

        // Assertions
        assertThat(exception.getMessage(), equalTo(
                String.format("Item with id %s not found when trying to update it", itemId)));
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(itemRepository, times(0))
                .save(any());
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(0))
                .findAllByItemIdIn(anyList());
    }


    @Test
    public void testGetById() {
        // Mock necessary dependencies and setup test data
        long itemId = 1L;
        long userId = 2L;

        User owner = getUser(1);
        User requestingUser = getUser(2);

        ItemRequest request = getItemRequest(1, requestingUser);
        Item targetItem = getItem(1, owner, request);

        List<Comment> comments = getCommentsForItem(targetItem, requestingUser);


        when(itemRepository.findById(itemId)).thenReturn(Optional.of(targetItem));
        when(commentRepository.findAllByItemIdIn(List.of(itemId))).thenReturn(comments);

        // Call the getById method
        ItemDto itemDto = itemService.getById(itemId, userId);

        // Assertions
        assertThat(itemDto, notNullValue());
        assertThat(targetItem.getName(), equalTo(itemDto.getName()));
        assertThat(targetItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(targetItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(request.getId(), equalTo((itemDto.getRequestId())));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());
        assertThat(itemDto.getRentCounter(), nullValue());
        verify(userService, times(1))
                .existsById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(1))
                .findAllByItemIdIn(anyList());
    }


    @Test
    public void testGetAllForUser() {
        // Mock necessary dependencies and setup test data
        long userId = 1L;
        User owner = getUser(1);
        User anotherUser = getUser(2);

        ItemRequest request1 = getItemRequest(1, anotherUser);

        Item item1 = getItem(1, owner, request1);
        Item item2 = getItem(2, owner, null);
        List<Item> itemList = List.of(item1, item2);

        Pageable page = mock(Pageable.class);
        List<BookingsByItem> bookings = getBookings(item1, anotherUser);
        List<Comment> comments = getCommentsForItem(item1, anotherUser);
        when(userService.findById(userId))
                .thenReturn(owner);
        when(itemRepository.findAllByOwnerId(owner.getId(), page))
                .thenReturn(itemList);
        when(bookingRepository.findDatesByItemId(
                eq(itemList.stream()
                        .map(Item::getId)
                        .collect(Collectors.toList())),
                any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(commentRepository.findAllByItemIdIn(
                itemList.stream()
                        .map(Item::getId)
                        .collect(Collectors.toList())))
                .thenReturn(comments);

        // Call the getAllForUser method
        List<ItemDto> itemDtos = itemService.getAllForUser(userId, page);

        // Assertions
        assertThat(itemDtos, notNullValue());
        assertThat(itemDtos, hasSize(itemList.size()));
        assertThat(itemDtos.get(0).getName(), equalTo(item1.getName()));
        assertThat(itemDtos.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemDtos.get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(itemDtos.get(0).getRequestId(), equalTo((item1.getRequest().getId())));
        assertThat(itemDtos.get(0).getLastBooking(), equalTo(getBookingShort(bookings.get(0).getLastBooking())));
        assertThat(itemDtos.get(0).getNextBooking(), equalTo(getBookingShort(bookings.get(0).getNextBooking())));
        assertThat(itemDtos.get(0).getRentCounter(), equalTo(bookings.get(0).getRentCounter()));
        assertThat(itemDtos.get(1).getLastBooking(), nullValue());
        assertThat(itemDtos.get(1).getName(), equalTo(item2.getName()));
        verify(userService, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findAllByOwnerId(anyLong(), any(Pageable.class));
        verify(bookingRepository, times(1))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(1))
                .findAllByItemIdIn(anyList());
        // Add more assertions as needed
    }

    @Test
    public void testSearchByNameAndDescr() {
        // Mock necessary dependencies and setup test data
        long userId = 1L;
        String text = "search text";

        User user = getUser(1);
        User owner = getUser(2);


        List<Item> foundItems = new ArrayList<>();
        Item item1 = getItem(1, owner, null);
        Item item2 = getItem(2, owner, null);
        foundItems.add(item1);
        foundItems.add(item2);

        Pageable page = mock(Pageable.class);

        List<Comment> comments = getCommentsForItem(item1, user);

        when(itemRepository.findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                text, text, page)).thenReturn(foundItems);
        when(commentRepository.findAllByItemIdIn(foundItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList())))
                .thenReturn(comments);

        // Call the searchByNameAndDescr method
        List<ItemDto> itemDtos = itemService.searchByNameAndDescr(text, userId, page);

        // Assertions
        assertThat(itemDtos, notNullValue());
        assertThat(itemDtos, hasSize(foundItems.size()));
        assertThat(itemDtos.get(0), hasProperty("name", equalTo(item1.getName())));
        assertThat(itemDtos.get(0).getName(), equalTo(item1.getName()));
        assertThat(itemDtos.get(0).getDescription(), equalTo(item1.getDescription()));
        assertThat(itemDtos.get(0).getAvailable(), equalTo(item1.getAvailable()));
        assertThat(itemDtos.get(0).getRequestId(), nullValue());
        assertThat(itemDtos.get(0).getLastBooking(), nullValue());
        assertThat(itemDtos.get(0).getNextBooking(), nullValue());
        assertThat(itemDtos.get(0).getRentCounter(), nullValue());
        assertThat(itemDtos.get(1).getLastBooking(), nullValue());
        assertThat(itemDtos.get(1).getName(), equalTo(item2.getName()));
        verify(userService, times(1))
                .existsById(anyLong());
        verify(itemRepository, times(1))
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        anyString(), anyString(), any(Pageable.class));
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(1))
                .findAllByItemIdIn(anyList());
    }

    @Test
    public void testSearchByEmptyNameAndDescr() {
        // Mock necessary dependencies and setup test data
        long userId = 1L;
        String text = "";


        Pageable page = mock(Pageable.class);


        // Call the searchByNameAndDescr method
        List<ItemDto> itemDtos = itemService.searchByNameAndDescr(text, userId, page);

        // Assertions
        assertThat(itemDtos, notNullValue());
        assertThat(itemDtos, hasSize(0));
        verify(userService, times(1))
                .existsById(anyLong());
        verify(itemRepository, times(0))
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        anyString(), anyString(), any(Pageable.class));
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(0))
                .findAllByItemIdIn(anyList());
    }

    @Test
    public void testSearchByNameAndDescrByNotFoundUser() {
        // Mock necessary dependencies and setup test data
        long userId = 1L;
        String text = "search text";
        Pageable page = mock(Pageable.class);

        doThrow(new NotFoundException(String.format(
                "User with id %s not found when trying to search for item by search text", userId)))
                .when(userService)
                .existsById(userId);

        // Call the searchByNameAndDescr method
        Exception exception = assertThrows(NotFoundException.class,
                () -> itemService.searchByNameAndDescr(text, userId, page));

        // Assertions
        assertThat(exception.getMessage(), equalTo(
                String.format("User with id %s not found when trying to search for item by search text", userId)));
        verify(userService, times(1))
                .existsById(anyLong());
        verify(itemRepository, times(0))
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        anyString(), anyString(), any(Pageable.class));
        verify(bookingRepository, times(0))
                .findDatesByItemId(anyList(), any(LocalDateTime.class));
        verify(commentRepository, times(0))
                .findAllByItemIdIn(anyList());
    }


    @Test
    public void testCreateComment() {
        // Mock necessary dependencies and setup test data
        long itemId = 1L;
        long userId = 1L;

        CommentDto commentDto = CommentDto.builder().text("Test text").build();

        User author = getUser(1);
        User owner = getUser(2);

        Item item = getItem(1, owner, null);

        when(userService.findById(userId))
                .thenReturn(author);
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(itemId), eq(userId), any(LocalDateTime.class), eq(APPROVED)))
                .thenReturn(true);

        when(commentRepository.save(any(Comment.class)))
                .then(returnsFirstArg());

        // Call the create method
        CommentDto createdCommentDto = itemService.create(commentDto, itemId, userId);

        // Assertions
        assertThat(createdCommentDto, notNullValue());
        assertThat(createdCommentDto.getId(), equalTo(commentDto.getId()));
        assertThat(createdCommentDto.getText(), equalTo(commentDto.getText()));
        verify(userService, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .existsByItemIdAndBookerIdAndEndBeforeAndStatus(any(), any(), any(), any());
        verify(commentRepository, times(1))
                .save(any());
        // Add more assertions as needed
    }

    @Test
    public void testCreateCommentWhenNotRented() {
        long itemId = 1L;
        long userId = 1L;

        CommentDto commentDto = CommentDto.builder().text("Test text").build();

        User author = getUser(1);
        User owner = getUser(2);

        Item item = getItem(1, owner, null);

        when(userService.findById(userId))
                .thenReturn(author);
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                eq(itemId), eq(userId), any(LocalDateTime.class), eq(APPROVED)))
                .thenReturn(false);

        // Call the create method
        Exception exception = assertThrows(BadRequestException.class,
                () -> itemService.create(commentDto, itemId, userId));

        // Assertions
        assertThat(exception.getMessage(), equalTo(
                String.format("User with id: %s did not rent item with id: %s to comment it", userId, itemId)));
        verify(userService, times(1))
                .findById(anyLong());
        verify(itemRepository, times(1))
                .findById(anyLong());
        verify(bookingRepository, times(1))
                .existsByItemIdAndBookerIdAndEndBeforeAndStatus(any(), any(), any(), any());
        verify(commentRepository, times(0))
                .save(any());
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

    private List<Comment> getCommentsForItem(Item item, User author) {
        return List.of(new Comment(1L, "Comment text", item, author));
    }

    private List<BookingsByItem> getBookings(Item item, User booker) {
        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setStart(now().minusDays(1));
        lastBooking.setEnd(now().minusHours(1));
        lastBooking.setStatus(APPROVED);
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setStart(now().plusHours(1));
        nextBooking.setEnd(now().plusDays(2));
        nextBooking.setStatus(Status.WAITING);
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        return List.of(new BookingsByItem(item.getId(), lastBooking, nextBooking, 5L));
    }

    private BookingShort getBookingShort(Booking booking) {
        return BookingShort.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .bookerId(booking.getBooker().getId())
                .build();
    }

}