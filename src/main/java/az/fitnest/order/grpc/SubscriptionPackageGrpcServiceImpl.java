package az.fitnest.order.grpc;

import az.fitnest.order.model.entity.PackageOption;
import az.fitnest.order.model.entity.SubscriptionPackage;
import az.fitnest.order.model.entity.PlanBenefit;
import az.fitnest.order.repository.SubscriptionPackageRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class SubscriptionPackageGrpcServiceImpl extends SubscriptionPackageServiceGrpc.SubscriptionPackageServiceImplBase {

    private final SubscriptionPackageRepository packageRepository;
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
        var packages = packageRepository.findByIsActiveTrue();
        GetGymPlansResponse.Builder responseBuilder = GetGymPlansResponse.newBuilder();
        for (SubscriptionPackage pkg : packages) {
            responseBuilder.addPackages(mapPackageToGrpc(pkg));
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkPlanExists(CheckPlanExistsRequest request, StreamObserver<CheckPlanExistsResponse> responseObserver) {
        var opt = packageRepository.findById(request.getPackageId());
        if (opt.isPresent()) {
            responseObserver.onNext(CheckPlanExistsResponse.newBuilder()
                    .setExists(true)
                    .setIsActive(opt.get().getIsActive() != null && opt.get().getIsActive())
                    .build());
        } else {
            responseObserver.onNext(CheckPlanExistsResponse.newBuilder()
                    .setExists(false)
                    .setIsActive(false)
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getPlansByIds(GetPlansByIdsRequest request, StreamObserver<GetPlansByIdsResponse> responseObserver) {
        try {
            var packages = packageRepository.findAllByIdWithOptions(request.getPackageIdsList());
            // Force initialization of benefits for each option
            for (SubscriptionPackage pkg : packages) {
                if (pkg.getOptions() != null) {
                    for (PackageOption option : pkg.getOptions()) {
                        option.getBenefits().size(); // initialize benefits
                    }
                }
            }
            GetPlansByIdsResponse.Builder responseBuilder = GetPlansByIdsResponse.newBuilder();
            for (SubscriptionPackage pkg : packages) {
                responseBuilder.addPackages(mapPackageToGrpc(pkg));
            }
            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to fetch plans by IDs: " + e.getMessage())
                .withCause(e)
                .asRuntimeException());
            return;
        }
        responseObserver.onCompleted();
    }

    private SubscriptionPackageInfo mapPackageToGrpc(SubscriptionPackage pkg) {
        SubscriptionPackageInfo.Builder pkgBuilder = SubscriptionPackageInfo.newBuilder()
                .setPackageId(pkg.getId())
                .setName(pkg.getName() != null ? pkg.getName() : "")
                .setCurrency(pkg.getCurrency() != null ? pkg.getCurrency() : "AZN")
                .setIsActive(pkg.getIsActive() != null && pkg.getIsActive());

        if (pkg.getOptions() != null) {
            for (PackageOption opt : pkg.getOptions()) {
                if (opt.getBenefits() != null) {
                    for (PlanBenefit benefit : opt.getBenefits()) {
                        if (benefit.getDescription() != null) {
                            String desc = benefit.getDescription();
                            if (!pkgBuilder.getBenefitsList().contains(desc)) {
                                pkgBuilder.addBenefits(desc);
                            }
                        }
                    }
                }
            }
        }

        if (pkg.getOptions() != null) {
            for (PackageOption opt : pkg.getOptions()) {
                SubscriptionPackageOption.Builder optBuilder = SubscriptionPackageOption.newBuilder()
                        .setDurationMonths(opt.getDurationMonths() != null ? opt.getDurationMonths() : 0)
                        .setPriceStandard(opt.getPriceStandard() != null ? opt.getPriceStandard().toPlainString() : "0")
                        .setPriceDiscounted(opt.getPriceDiscounted() != null ? opt.getPriceDiscounted().toPlainString() : "")
                        .setEntryLimit(opt.getEntryLimit() != null ? opt.getEntryLimit() : 0)
                        .setFreezeDays(opt.getFreezeDays() != null ? opt.getFreezeDays() : 0);

                if (opt.getServices() != null) {
                    for (az.fitnest.order.model.entity.PlanService svc : opt.getServices()) {
                        optBuilder.addServices(svc != null && svc.getName() != null ? svc.getName() : "");
                    }
                }
                pkgBuilder.addOptions(optBuilder.build());
            }
        }
        return pkgBuilder.build();
    }
}
