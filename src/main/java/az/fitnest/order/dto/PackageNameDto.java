package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
public record PackageNameDto(
    Long id,
    String name
) {}
