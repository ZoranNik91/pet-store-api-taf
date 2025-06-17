package zoran.utils;

import com.github.javafaker.Faker;
import zoran.models.User;

public class UserGenerator {
    private static final Faker faker = new Faker();
    
    public static long generateRandomId() {
        return faker.number().randomNumber(6, true);
    }
    
    public static User generateRandomUser() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String username = (firstName + "." + lastName).toLowerCase();
        
        return User.builder()
                .id(generateRandomId())
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(faker.internet().emailAddress(username))
                .password(faker.internet().password(8, 16, true, true, true))
                .phone(faker.phoneNumber().phoneNumber())
                .userStatus(faker.number().numberBetween(0, 2))
                .build();
    }
}
