package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareItApp {

    public static void main(String[] args) {
        SpringApplication.run(ShareItApp.class, args);
    }
    //jdbc:h2:mem:shareit
    /*logging:
    level:
        org:
            springframework:
                orm:
                    jpa: INFO
                    jpa.JpaTransactionManager: DEBUG
                transaction: INFO
                transaction.interceptor: TRACE
        ru:
            practicum:
                shareit: DEBUG
spring:
    config:
        activate:
            on-profile: ci,test
    datasource:
        driverClassName: org.h2.Driver
        password: test
        url: jdbc:h2:file:./db/filmorate
        username: test
    jpa:
        show-sql: true
        hibernate:
            ddl-auto: none
        properties:
            hibernate:
                dialect: org.hibernate.dialect.PostgreSQL10Dialect
                format_sql: true
    sql:
        init:
            mode: always
*/

}
