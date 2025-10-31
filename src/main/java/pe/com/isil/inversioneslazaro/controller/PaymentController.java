package pe.com.isil.inversioneslazaro.controller; // Asegúrate que sea tu paquete

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentController {

    // 1. Configura tu API Key (¡Mantenla secreta!)
    // Es mejor poner esto en 'application.properties' y usar @Value
    private String secretKey = "sk_test_TU_CLAVE_SECRETA_DE_STRIPE_AQUI";

    @PostConstruct // Esto asegura que la clave se configure al iniciar
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Este es el MÉTODO que maneja la petición.
     * Recibe un JSON desde el frontend que debe contener el "token".
     */
    @PostMapping("/api/pago/crear-cargo")
    public Map<String, Object> crearCargo(@RequestBody Map<String, String> request) {

        // 2. Obtenemos el token que envió el JavaScript
        String token = request.get("token");

        // 3. Preparamos la respuesta que daremos al frontend
        Map<String, Object> response = new HashMap<>();

        try {
            // 4. Toda tu lógica de pago va DENTRO del método
            Map<String, Object> params = new HashMap<>();
            params.put("amount", 10000); // S/ 100.00 (en centavos)
            params.put("currency", "pen");
            params.put("description", "Pedido #12345");
            params.put("source", token); // Usamos el token recibido

            Charge charge = Charge.create(params);

            // 5. Enviamos una respuesta exitosa
            response.put("status", charge.getStatus());
            response.put("chargeId", charge.getId());

        } catch (StripeException e) {
            // 6. Manejamos cualquier error de Stripe
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }
}