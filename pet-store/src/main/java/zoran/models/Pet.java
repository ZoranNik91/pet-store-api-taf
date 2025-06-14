package zoran.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Pet {
    private Long id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status;
}

@Data
@Builder
class Category {
    private Long id;
    private String name;
}

@Data
@Builder
class Tag {
    private Long id;
    private String name;
}