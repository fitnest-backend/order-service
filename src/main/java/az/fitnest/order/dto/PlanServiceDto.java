package az.fitnest.order.dto;

import lombok.Builder;

@Builder
public record PlanServiceDto(
    Long id,
    String name
) {}
