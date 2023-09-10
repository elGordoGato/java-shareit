/*
package ru.practicum.shareit.controllers;

*/
/*
import java.io.IOException;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureJsonTesters
@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    public static MockWebServer mockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
BookingClient bookingClient;

    @Autowired
    private JacksonTester<BookItemRequestDto> jsonRequest;


    @BeforeAll
    static void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start(9090);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockServer.getPort());
        System.out.println(baseUrl);
    }

    @Test
    public void testCreateBooking() throws Exception {
        // Arrange
        BookItemRequestDto bookingRequest = new BookItemRequestDto(1L,
                LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusDays(1));

        mockServer.enqueue(new MockResponse());

        // Act and Assert
        mockMvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest.write(bookingRequest).getJson()));

        RecordedRequest recordedRequest = mockServer.takeRequest();

        assertThat("GET", equalTo(recordedRequest.getMethod()));
        assertThat("/employee/100", equalTo(recordedRequest.getPath()));

    }
*//*


 */
/*    @Test
    public void testCreateBookingThrowsConflict() throws Exception {
        // Arrange
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .booker(new UserId(1L))
                .item(new ItemShort(1L, "Test Item"))
                .status(Status.WAITING)
                .build();
        given(bookingService.create(any(), anyLong()))
                .willThrow(new ConflictException("Conflict Exception Test"));

        // Act and Assert
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest.write(bookingRequest).getJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Conflict'].length()", is(1)))
                .andExpect(jsonPath("$.['Conflict']")
                        .value("Conflict Exception Test"));
    }


    @Test
    void testCreateBookingInvalidDateOrder() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest.write(bookingRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("End date should be after start date"));
    }

    @Test
    void testCreateBookingMissingHeader() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest.write(bookingRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("Required request header 'X-Sharer-User-Id' " +
                                "for method parameter type long is not present"));
    }

    @Test
    void testCreateInvalidBooking() throws Exception {
        BookingRequest bookingRequest = BookingRequest.builder()
                .start(LocalDateTime.now().minusMinutes(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest.write(bookingRequest).getJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(3)))
                .andExpect(jsonPath("$.['Bad Request']",
                        containsInAnyOrder("must be a date in the present or in the future",
                                "must not be null",
                                "must not be null")));
    }

    @Test
    public void testApproveBooking() throws Exception {
        // Arrange
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .booker(new UserId(1L))
                .item(new ItemShort(1L, "Test Item"))
                .status(Status.APPROVED)
                .build();
        given(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .willReturn(bookingDto);

        // Act and Assert
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonDto.write(bookingDto).getJson()));
    }

    @Test
    void approveBookingNotFoundTest() throws Exception {
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        when(bookingService.approve(bookingId, userId, approved))
                .thenThrow(new NotFoundException("Booking not found"));

        // Act and Assert
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Not Found']")
                        .value("Booking not found"));
    }


    @Test
    void approveBookingForbiddenTest() throws Exception {
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        when(bookingService.approve(bookingId, userId, approved))
                .thenThrow(new ForbiddenException("No rights to approve"));

        // Act and Assert
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Forbidden']")
                        .value("No rights to approve"));
    }


    @Test
    void approveBookingAlreadyApprovedTest() throws Exception {
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        when(bookingService.approve(bookingId, userId, approved))
                .thenThrow(new BadRequestException("This booking already approved"));

        // Act and Assert
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", String.valueOf(approved)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("This booking already approved"));
    }


    // Тестирование метода getById
    @Test
    public void testGetBookingById() throws Exception {
        // Arrange
        long bookingId = 1L;
        long userId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .booker(new UserId(1L))
                .item(new ItemShort(1L, "Test Item"))
                .status(Status.WAITING)
                .build();
        given(bookingService.getById(anyLong(), anyLong()))
                .willReturn(bookingDto);

        // Act and Assert
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonDto.write(bookingDto).getJson()));
    }

    @Test
    void getBookingByIdNotFoundTest() throws Exception {
        long bookingId = 1L;
        long userId = 1L;
        when(bookingService.getById(bookingId, userId))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Not Found']")
                        .value("Booking not found"));
    }

    // Тестирование метода getAllForBooker
    @Test
    public void testGetAllBookingsForBooker() throws Exception {
        // Arrange
        long userId = 1L;
        String state = "ALL";
        int from = 0;
        int size = 10;
        List<BookingDto> bookingDtos = Arrays.asList(
                BookingDto.builder().id(1L)
                        .start(LocalDateTime.now())
                        .end(LocalDateTime.now().plusDays(1))
                        .booker(new UserId(1L))
                        .item(new ItemShort(1L, "Test Item1"))
                        .status(Status.WAITING)
                        .build(),
                BookingDto.builder().id(2L)
                        .start(LocalDateTime.now().plusHours(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .booker(new UserId(2L))
                        .item(new ItemShort(3L, "Test Item2"))
                        .status(Status.APPROVED)
                        .build(),
                BookingDto.builder().id(3L)
                        .start(LocalDateTime.now().plusHours(5))
                        .end(LocalDateTime.now().plusDays(5))
                        .booker(new UserId(4L))
                        .item(new ItemShort(5L, "Test Item3"))
                        .status(Status.REJECTED)
                        .build());
        given(bookingService.getAllForUserByState(anyLong(), any(), anyBoolean(), any()))
                .willReturn(bookingDtos);

        // Act and Assert
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonListDto.write(bookingDtos).getJson()));
    }

    @Test
    void getAllBookingsForBookerInvalidStateTest() throws Exception {
        long userId = 1L;
        String state = "INVALID_STATE";
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.error")
                        .value("Unknown state: " + state));
    }

    @Test
    void getAllBookingsForBookerInvalidPagingTest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = -1;
        int size = 0;

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForBooker.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForBooker.size: must be greater than 0")));
    }


    @Test
    void getAllBookingsForOwnerTest() throws Exception {
        // Arrange
        long userId = 1L;
        String state = "WAITING";
        int from = 0;
        int size = 10;
        List<BookingDto> bookingDtos = Arrays.asList(
                BookingDto.builder().id(1L)
                        .start(LocalDateTime.now())
                        .end(LocalDateTime.now().plusDays(1))
                        .booker(new UserId(1L))
                        .item(new ItemShort(1L, "Test Item1"))
                        .status(Status.WAITING)
                        .build(),
                BookingDto.builder().id(2L)
                        .start(LocalDateTime.now().plusHours(1))
                        .end(LocalDateTime.now().plusDays(3))
                        .booker(new UserId(2L))
                        .item(new ItemShort(3L, "Test Item2"))
                        .status(Status.APPROVED)
                        .build(),
                BookingDto.builder().id(3L)
                        .start(LocalDateTime.now().plusHours(5))
                        .end(LocalDateTime.now().plusDays(5))
                        .booker(new UserId(4L))
                        .item(new ItemShort(5L, "Test Item3"))
                        .status(Status.REJECTED)
                        .build());
        given(bookingService.getAllForUserByState(anyLong(), any(), anyBoolean(), any()))
                .willReturn(bookingDtos);

        // Act and Assert
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonListDto.write(bookingDtos).getJson()));
    }

    @Test
    void getAllBookingsForOwnerInvalidStateTest() throws Exception {
        long userId = 1L;
        String state = "WRONG_STATE";
        int from = 0;
        int size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$.['Bad Request']")
                        .value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.error")
                        .value("Unknown state: " + state));
    }

    @Test
    void getAllBookingsForOwnerInvalidPagingTest() throws Exception {
        long userId = 1L;
        String state = "ALL";
        int from = -10;
        int size = -4;

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].length()", is(1)))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForOwner.from: must be greater than or equal to 0")))
                .andExpect(jsonPath("$.['Bad Request'].[0]",
                        containsString("getAllForOwner.size: must be greater than 0")));
    }*//*


}*/