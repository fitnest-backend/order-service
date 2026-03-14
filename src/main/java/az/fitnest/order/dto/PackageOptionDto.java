package az.fitnest.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Builder
public record PackageOptionDto(
    @JsonProperty("option_id")
    Long optionId,
    @JsonProperty("duration_months")
    Integer durationMonths,
    PackagePriceDto price,
    String badge,
    @JsonProperty("visit_limit")
    Integer visitLimit,
    @JsonProperty("freeze_days")
    Integer freezeDays,
    List<PackageServiceDto> services,
    List<PackageBenefitDto> benefits
) {}
