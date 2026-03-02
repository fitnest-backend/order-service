package az.fitnest.order.dto;

import lombok.Builder;

@Builder
public record PackageDurationDto(
    Integer durationMonths
) {}
