package az.fitnest.order.dto;

import lombok.Builder;

@Builder
public record PackageNameDto(
    Long id,
    String name
) {}
