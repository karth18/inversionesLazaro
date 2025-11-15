package pe.com.isil.inversioneslazaro.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Crea un cargo (pago) en Stripe.
     */
    public Charge createCharge(int amount, String currency, String description,
                               String sourceToken, Map<String, String> metadata) throws StripeException {

        Map<String, Object> chargeParams = Map.of(
                "amount", amount,
                "currency", currency,
                "description", description,
                "source", sourceToken,
                "metadata", metadata
        );
        return Charge.create(chargeParams);
    }

    /**
     * ¡NUEVO! Crea un reembolso (devolución) en Stripe.
     */
    public Refund createRefund(String chargeId) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(chargeId)
                .build();
        return Refund.create(params);
    }
}