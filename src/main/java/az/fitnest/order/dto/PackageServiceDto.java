package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PackageServiceDto(
    @JsonProperty("service_name")
    String serviceName
) {}
