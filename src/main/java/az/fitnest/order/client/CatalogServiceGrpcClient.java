package az.fitnest.order.client;

import az.fitnest.catalog.grpc.GymServiceGrpc;
import az.fitnest.catalog.grpc.GymSupportsPlanRequest;
import az.fitnest.catalog.grpc.GymSupportsPlanResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceGrpcClient {
    @GrpcClient("catalog-backend")
    private GymServiceGrpc.GymServiceBlockingStub blockingStub;

    public boolean gymSupportsPlan(Long gymId, Long planId) {
        GymSupportsPlanRequest request = GymSupportsPlanRequest.newBuilder()
                .setGymId(gymId)
                .setPlanId(planId)
                .build();
        GymSupportsPlanResponse response = blockingStub.gymSupportsPlan(request);
        return response.getSupported();
    }
}
