package az.fitnest.order.grpc;

import az.fitnest.order.entity.DurationOption;
import az.fitnest.order.entity.MembershipPlan;
import az.fitnest.order.entity.PlanBenefit;
import az.fitnest.order.repository.MembershipPlanRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class MembershipPlanGrpcServiceImpl extends MembershipPlanServiceGrpc.MembershipPlanServiceImplBase {

    private final MembershipPlanRepository planRepository;
    private final az.fitnest.order.service.impl.UserSubscriptionService userSubscriptionService;

    @Override
    public void checkIn(az.fitnest.order.grpc.CheckInRequest request, StreamObserver<az.fitnest.order.grpc.CheckInResponse> responseObserver) {
        try {
            boolean success = userSubscriptionService.checkIn(request.getUserId(), request.getGymId());
            responseObserver.onNext(az.fitnest.order.grpc.CheckInResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage("Checked in successfully")
                    .build());
        } catch (Exception e) {
            responseObserver.onNext(az.fitnest.order.grpc.CheckInResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(e.getMessage() != null ? e.getMessage() : "Check-in failed")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getGymPlans(GetGymPlansRequest request, StreamObserver<GetGymPlansResponse> responseObserver) {
        long gymId = request.getGymId();

        var plans = planRepository.findByGymId(gymId);

        GetGymPlansResponse.Builder responseBuilder = GetGymPlansResponse.newBuilder();

        for (MembershipPlan plan : plans) {
            GymMembershipPlan.Builder planBuilder = GymMembershipPlan.newBuilder()
                    .setPlanId(plan.getId())
                    .setName(plan.getName() != null ? plan.getName() : "")
                    .setCurrency(plan.getCurrency() != null ? plan.getCurrency() : "AZN")
                    .setIsActive(plan.getIsActive() != null && plan.getIsActive());

            // Map benefits
            if (plan.getBenefits() != null) {
                for (PlanBenefit benefit : plan.getBenefits()) {
                    if (benefit.getDescription() != null) {
                        planBuilder.addBenefits(benefit.getDescription());
                    }
                }
            }

            // Map duration options
            if (plan.getOptions() != null) {
                for (DurationOption opt : plan.getOptions()) {
                    PlanDurationOption.Builder optBuilder = PlanDurationOption.newBuilder()
                            .setDurationMonths(opt.getDurationMonths() != null ? opt.getDurationMonths() : 0)
                            .setPriceStandard(opt.getPriceStandard() != null ? opt.getPriceStandard().toPlainString() : "0")
                            .setPriceDiscounted(opt.getPriceDiscounted() != null ? opt.getPriceDiscounted().toPlainString() : "")
                            .setEntryLimit(opt.getEntryLimit() != null ? opt.getEntryLimit() : 0)
                            .setFreezeDays(opt.getFreezeDays() != null ? opt.getFreezeDays() : 0);

                    if (opt.getServices() != null) {
                        for (String svc : opt.getServices()) {
                            optBuilder.addServices(svc != null ? svc : "");
                        }
                    }
                    planBuilder.addOptions(optBuilder.build());
                }
            }

            responseBuilder.addPlans(planBuilder.build());
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
