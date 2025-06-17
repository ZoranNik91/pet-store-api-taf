package zoran.utils;

import com.github.javafaker.Faker;
import zoran.models.Order;

import java.time.OffsetDateTime;

public class OrderGenerator {
    private static final Faker faker = new Faker();
    
    public static long generateRandomId() {
        return faker.number().randomNumber(6, true);
    }
    
    public static Order generateRandomOrder() {
        return Order.builder()
                .id(generateRandomId())
                .petId(faker.number().randomNumber(6, true))
                .quantity(faker.number().numberBetween(1, 10))
                .shipDate(OffsetDateTime.now().plusDays(faker.number().numberBetween(1, 30)))
                .status("placed")
                .complete(faker.bool().bool())
                .build();
    }
}
