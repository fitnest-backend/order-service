package az.fitnest.order.dto;

import lombok.Builder;

@Builder
public record PackageBenefitDto(
    String logo,
    String description
) {}
