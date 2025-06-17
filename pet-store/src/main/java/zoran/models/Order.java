

package zoran.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * An order for a pet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("petId")
    private Long petId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("shipDate")
    private OffsetDateTime shipDate;
    
    /**
     * Order Status
     * Enum: [placed, approved, delivered]
     */
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("complete")
    private Boolean complete;
}
