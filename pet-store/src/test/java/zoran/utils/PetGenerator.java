package zoran.utils;

import zoran.models.Pet;
import zoran.models.Pet.Category;
import zoran.models.Pet.Tag;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating test Pet objects with random data.
 */
public class PetGenerator {
    private static final Random RANDOM = new Random();
    private static final List<String> PET_NAMES = Arrays.asList(
        "Buddy", "Max", "Bella", "Charlie", "Lucy", "Cooper", "Luna", "Rocky", "Zoe", "Bear"
    );
    
    private static final List<String> PET_CATEGORIES = Arrays.asList(
        "Dogs", "Cats", "Birds", "Fish", "Reptiles", "Small Animals"
    );
    
    private static final List<String> PET_TAGS = Arrays.asList(
        "cute", "playful", "friendly", "energetic", "calm", "loyal", "intelligent", "curious"
    );
    
    private static final List<String> PET_STATUSES = Arrays.asList(
        "available", "pending", "sold"
    );
    
    /**
     * Generates a random pet ID between 1 and 1,000,000.
     * Note: In a real application, you might want to ensure uniqueness.
     * 
     * @return A random pet ID
     */
    public static long generateRandomId() {
        return RANDOM.nextInt(1_000_000) + 1;
    }
    
    /**
     * Generates a random pet with realistic test data.
     * 
     * @return A new Pet object with random data
     */
    public static Pet generateRandomPet() {
        // Create a new pet with random data
        Pet pet = new Pet();
        pet.setId(generateRandomId());
        pet.setName(getRandomName());
        pet.setStatus(getRandomStatus());
        
        // Set a random category
        Category category = new Category();
        category.setId((long) RANDOM.nextInt(100) + 1);
        category.setName(getRandomCategory());
        pet.setCategory(category);
        
        // Add some photo URLs
        pet.setPhotoUrls(Arrays.asList(
            "https://example.com/pet" + pet.getId() + "_1.jpg",
            "https://example.com/pet" + pet.getId() + "_2.jpg"
        ));
        
        // Add some random tags
        pet.setTags(Arrays.asList(
            createRandomTag(),
            createRandomTag()
        ));
        
        return pet;
    }
    
    private static String getRandomName() {
        return PET_NAMES.get(RANDOM.nextInt(PET_NAMES.size()));
    }
    
    private static String getRandomCategory() {
        return PET_CATEGORIES.get(RANDOM.nextInt(PET_CATEGORIES.size()));
    }
    
    private static String getRandomStatus() {
        return PET_STATUSES.get(RANDOM.nextInt(PET_STATUSES.size()));
    }
    
    private static Tag createRandomTag() {
        Tag tag = new Tag();
        tag.setId((long) (RANDOM.nextInt(100) + 1));
        tag.setName(PET_TAGS.get(RANDOM.nextInt(PET_TAGS.size())));
        return tag;
    }
}
