package zoran.utils;

import com.github.javafaker.Faker;
import zoran.models.Pet;
import zoran.models.Pet.Category;
import zoran.models.Pet.Tag;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PetGenerator {
    private static final Faker faker = new Faker();

    /**
     * Generates a random Pet with all required fields
     */
    public static Pet generateRandomPet() {
        return Pet.builder()
                .id(generateRandomId())
                .category(generateRandomCategory())
                .name(generateRandomPetName())
                .photoUrls(generateRandomPhotoUrls())
                .tags(generateRandomTags())
                .status(generateRandomStatus())
                .build();
    }

    /**
     * Generates a random ID (positive long)
     */
    public static long generateRandomId() {
        return faker.number().randomNumber(5, false);
    }

    /**
     * Generates a random pet name
     */
    public static String generateRandomPetName() {
        return faker.dog().name();
    }

    /**
     * Generates a random pet status
     */
    public static String generateRandomStatus() {
        return faker.options().option("available", "pending", "sold");
    }

    /**
     * Generates a random category
     */
    public static Category generateRandomCategory() {
        return Category.builder()
                .id(faker.number().randomNumber(3, false))
                .name(faker.dog().breed())
                .build();
    }

    /**
     * Generates 1-3 random photo URLs
     */
    public static List<String> generateRandomPhotoUrls() {
        return IntStream.range(0, faker.number().numberBetween(1, 4))
                .mapToObj(i -> faker.internet().image())
                .collect(Collectors.toList());
    }

    /**
     * Generates 1-3 random tags
     */
    public static List<Tag> generateRandomTags() {
        return IntStream.range(0, faker.number().numberBetween(1, 4))
                .mapToObj(i -> Tag.builder()
                        .id(faker.number().randomNumber(2, false))
                        .name(faker.dog().memePhrase())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Generates a basic pet with only required fields
     */
    public static Pet generateBasicPet() {
        return Pet.builder()
                .id(generateRandomId())
                .name(generateRandomPetName())
                .status(generateRandomStatus())
                .build();
    }
}