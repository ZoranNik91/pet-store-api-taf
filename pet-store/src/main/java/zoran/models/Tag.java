package zoran.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A tag for a pet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}