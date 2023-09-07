DROP ALL OBJECTS;


CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name              VARCHAR(255)                            NOT NULL,
    email             VARCHAR(512)                            NOT NULL,
    registration_date TIMESTAMP                               NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);


CREATE TABLE IF NOT EXISTS requests
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id   BIGINT        NOT NULL,
    description VARCHAR(1000) NOT NULL,
    created     TIMESTAMP     NOT NULL,
    CONSTRAINT fk_requests_to_users FOREIGN KEY (author_id) REFERENCES users (id)
);


CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id    BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    available   BOOLEAN      NOT NULL,
    created     TIMESTAMP    NOT NULL,
    request_id  BIGINT,
    CONSTRAINT fk_items_to_users FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_items_to_requests FOREIGN KEY (request_id) REFERENCES requests (id)
);


CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id    BIGINT      NOT NULL,
    start_date TIMESTAMP   NOT NULL,
    end_date   TIMESTAMP   NOT NULL,
    booker_id  BIGINT      NOT NULL,
    status     VARCHAR(50) NOT NULL,
    CONSTRAINT items FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT users FOREIGN KEY (booker_id) REFERENCES users (id)
);


CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id   BIGINT        NOT NULL,
    author_id BIGINT        NOT NULL,
    text      VARCHAR(1000) NOT NULL,
    created   TIMESTAMP     NOT NULL,
    CONSTRAINT fk_comments_to_items FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id) REFERENCES users (id)
);