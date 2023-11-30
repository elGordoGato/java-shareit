# _Java - Share It_

**Share It** - sharing application for common items you need for daily routine.
- share you tools, equipment and any other stuff you are ready to give for some period of time
- search and use what others have shared
- enjoy your experience

<details><summary>The Concept</summary>
Sharing as a collaborative economy is gaining more and more popularity. 
If in 2014 the global sharing market was valued at only $15 billion, by 2025 it could reach $335 billion.

Why sharing is so popular. Imagine that at a Sunday fair you bought a few paintings and want to hang them at home. 
But here’s the problem - you need a drill for that, and you don’t have one. You can, of course, go to the store and buy one, 
but there is little sense in such a purchase - after you hang the paintings, the drill will just gather dust in the closet. 
You can invite a master - but you will have to pay for his services. And then you remember that you saw a drill at a friend’s place. 
The idea suggests itself - to borrow it. 

It’s a great luck that you had a friend with a drill and you remembered him right away! 
Otherwise, in search of a tool, you would have to write to all your friends and acquaintances. 
Or go back to the first two options - buying a drill or hiring a master. How much more convenient it would be 
if there was a service where users share things!

#### What this service are able to do:
It can provide users, first of all, 
with the opportunity to tell what things they are willing to share, and secondly, 
to find the necessary thing and rent it for some time. 
The service can not only allow you to book an item for certain dates, 
but also close access to it for the duration of the booking from other interested parties. 
In case the required item is not available on the service, users are able to leave requests. 
Maybe an ancient gramophone, which is strange to even offer for rent, 
will suddenly be needed for a theatrical production. New items for sharing can be added by request.
</details>

### _Technologies used_
REST API service with Java 11, Spring Boot, JPA Hibernate, PostgreSQL, Lombok, Docker.

### _Project Structure_
There are 2 microservices made as modules in project:

* **Gateway** - works as proxy processing and validating income requests and redirecting them to main server.
Depends on ShareIt-server. Runs on port 8080.


* **Server** - main service makes all the work and application logic. Uses Postgres database.
Runs on port 9090. 

<details><summary>Here are the main scenarios and endpoints:</summary>

- `POST /items` - Adding a new item. An `ItemDto` object is passed as input. `userId` in the `X-Sharer-User-Id` header is the identifier of the user who is adding the item. 
This user is the owner of the item. The owner’s identifier will be passed as input in each of the requests discussed below.


- `PATCH /items/{itemId}` - Editing an item. You can change the name, description and access status to the rental. 
Only the owner can edit the item. 


- `GET /items/{itemId}` - Viewing information about a specific item by its identifier. 
Any user can view information about the item. 


- `GET /items` - Viewing by the owner a list of all his items with the name and description for each. 


- `GET /items/search?text={text}` - Searching for an item by a potential tenant. The user passes the text in the query string, 
and the system searches for items that contain this text in the `name` or `description`. 
`text` is the text to search for. The search returns only items available for rent.


- `POST /bookings` - Adding a new booking request. A request can be created by any user
and then confirmed by the owner of the item. 
After creation, the request is in the `WAITING` status - “waiting for confirmation”.


- `PATCH /bookings/{bookingId}?approved={approved}` - Confirmation or rejection of a booking request. Can only be done by the owner of the item. 
Then the booking status becomes either `APPROVED` or `REJECTED`. 
The `approved` parameter can take values `true` or `false`. 


-`GET /bookings/{bookingId}` - Getting data about a specific booking (including its status). Can be done either by the author of the booking
or by the owner of the item to which the booking relates.


- `GET /bookings?state={state}` - Getting a list of all bookings for the current user. The `state` parameter is optional and defaults to `ALL`. 
It can also take values `CURRENT` (“current”), `PAST` (“completed”), `FUTURE` (“future”),
`WAITING` (“waiting for confirmation”), `REJECTED` (“rejected”). 
Bookings should be returned sorted by date from newer to older. 


- `GET /bookings/owner?state={state}` - Getting a list of bookings for all items of the current user. 
This request makes sense for the owner of at least one item. 
The work of the `state` parameter is similar to its work in the previous scenario.


- `POST /requests` - add a new request for an item. The main part of the request is the request text, 
where the user describes what kind of item they need. 


- `GET /requests` - get a list of your requests along with data about the responses to them. 
For each request, the following information should be provided: description, date and time of creation, 
and a list of responses in the format: item `id`, `name`, its `description`, as well as the `requestId`
and the `available` flag of the item. This way, using the specified item `id`, you can get detailed 
information about each item later. Requests should be returned in sorted order from newer to older. 


- `GET /requests/all?from={from}&size={size}` - get a list of requests created by other users. With this endpoint, 
users will be able to view existing requests that they could respond to. Requests are sorted by creation date:
from newer to older. The results should be returned in pages. To do this, you need to pass two parameters:
`from` - the index of the first element, starting from 0, and `size` - the number of elements to display. 


- `GET /requests/{requestId}` - get data about one specific request 
along with data about the responses to it in the same format as in the `GET /requests` endpoint. 
Any user can view data about a separate request.
</details>


### _Starting the service_
CLI start command: docker-compose -p shareit up

*Manual start-up*:

1. Set environmental variables.
2. Run database.
3. Run shareit-server.
4. Run shareit-gateway.

<details><summary>Environmental variables set by default in docker-compose:</summary>

  gateway:
  - SHAREIT_SERVER_URL=http://server:9090

  server:
  - DB_NAME=shareit
  - POSTGRES_USER=root
  - POSTGRES_PASSWORD=root
  - DB_HOST=db
  - DB_PORT=5432

  db:
  - POSTGRES_DB=shareit
  - POSTGRES_USER=root
  - POSTGRES_PASSWORD=root
</details>