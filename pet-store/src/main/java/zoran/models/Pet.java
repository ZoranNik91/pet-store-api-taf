package zoran.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A pet for sale in the pet store
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pet {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("category")
    private Category category;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("photoUrls")
    private List<String> photoUrls;
    
    @JsonProperty("tags")
    private List<Tag> tags;
    
    /**
     * pet status in the store
     * Enum: [available, pending, sold]
     */
    @JsonProperty("status")
    private String status;

    /**
     * A category for a pet
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Category {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
    }

    /**
     * A tag for a pet
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tag {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
    }
}