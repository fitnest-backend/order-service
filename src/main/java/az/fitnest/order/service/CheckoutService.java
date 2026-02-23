package az.fitnest.order.service;

import az.fitnest.order.dto.CheckoutRequest;
import az.fitnest.order.dto.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse processCheckout(Long userId, CheckoutRequest request);
}
