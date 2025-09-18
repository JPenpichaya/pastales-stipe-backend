package pastales.back.website;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class Controller {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Initialize Stripe API key
    static {
        Stripe.apiKey = "sk_test_51POM3TP3M4EMXc3BgEF0CRF1pRjuM1rKfmad36toaf9nUbKdnvdcChoxRTnUFowflLCvl3zJ3NBKEilah1aIgRmD00FFmUJG5d";
    }

    // Endpoint to create a checkout session
    @PostMapping("/create-checkout-session")
    public String createCheckoutSession(@RequestBody PriceModel data) {
        String YOUR_DOMAIN = "http://localhost:5173";

        List<SessionCreateParams.CustomField> customFieldList = new ArrayList<>();

        SessionCreateParams.CustomField customField1 =
                SessionCreateParams.CustomField.builder()
                        .setKey("birthdate")
                        .setLabel(
                                SessionCreateParams.CustomField.Label.builder()
                                        .setType(SessionCreateParams.CustomField.Label.Type.CUSTOM)
                                        .setCustom("วัน/เดือน/ปี (วันจันทร์ - อาทิตย์)")
                                        .build()
                        )
                        .setType(SessionCreateParams.CustomField.Type.TEXT)
                        .build();

        customFieldList.add(customField1);

        SessionCreateParams.CustomField customField2 =
                SessionCreateParams.CustomField.builder()
                        .setKey("story")
                        .setLabel(
                                SessionCreateParams.CustomField.Label.builder()
                                        .setType(SessionCreateParams.CustomField.Label.Type.CUSTOM)
                                        .setCustom("เรื่องราวที่อยากให้การ์ดนำทางเป็นพิเศษ")
                                        .build()
                        )
                        .setType(SessionCreateParams.CustomField.Type.TEXT)
                        .build();

        customFieldList.add(customField2);

        SessionCreateParams.CustomField customField3 =
                SessionCreateParams.CustomField.builder()
                        .setKey("words")
                        .setLabel(
                                SessionCreateParams.CustomField.Label.builder()
                                        .setType(SessionCreateParams.CustomField.Label.Type.CUSTOM)
                                        .setCustom("คำที่อยากจะให้กับคนที่พิเศษที่สุดในชีวิต")
                                        .build()
                        )
                        .setType(SessionCreateParams.CustomField.Type.TEXT)
                        .build();

        customFieldList.add(customField3);

        String priceId = data.getPrice();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setReturnUrl(YOUR_DOMAIN + "/return?session_id={CHECKOUT_SESSION_ID}")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPrice(priceId) // Replace with your Price ID
                                        .build())
                        .addAllCustomField(customFieldList)
                        .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                        .setShippingAddressCollection(
                                SessionCreateParams.ShippingAddressCollection.builder()
                                        .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.TH)
                                        .build())
                        .setPhoneNumberCollection(
                                SessionCreateParams.PhoneNumberCollection.builder()
                                        .setEnabled(true)
                                        .build())
                        .build();

        Map<String, String> map = new HashMap();
        Session session = null;
        try {
            session = Session.create(params);
            map.put("clientSecret", session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating session: " + e.getMessage();
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    @PostMapping("/create-booking-session")
    public String createBookingSession(@RequestBody PriceModel data) {
        String YOUR_DOMAIN = "http://localhost:5173";

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setReturnUrl(YOUR_DOMAIN + "/return-booking?session_id={CHECKOUT_SESSION_ID}")
                        .setSubmitType(SessionCreateParams.SubmitType.BOOK)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPrice(data.getPrice()) // Replace with your Price ID
                                        .build())
                        .build();
        Map<String, String> map = new HashMap();
        Session session = null;
        try {
            session = Session.create(params);
            map.put("clientSecret", session.getRawJsonObject().getAsJsonPrimitive("client_secret").getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating session: " + e.getMessage();
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    // Endpoint to get session status
    @GetMapping("/session-status")
    public Map<String, String> getSessionStatus(@RequestParam String session_id) throws StripeException {
        Session session = Session.retrieve(session_id);

        Map<String, String> responseMap = new HashMap<>();
        JsonObject sessionJson = session.getRawJsonObject();

        responseMap.put("status", sessionJson.getAsJsonPrimitive("status").getAsString());
        responseMap.put("customer_email", sessionJson
                .getAsJsonObject("customer_details")
                .getAsJsonPrimitive("email")
                .getAsString());

        return responseMap;
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile() throws IOException {
        // Replace with the actual path to your file
        File file = new File("E:\\work\\pastales\\S__6602852_0.jpg");

        // Check if the file exists
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Set content type as application/pdf or appropriate type
        MediaType mediaType = MediaType.APPLICATION_PDF;

        // Create response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(file.length());
        headers.setContentDispositionFormData("attachment", file.getName());

        // Create InputStreamResource from the file
        InputStream inputStream = new FileInputStream(file);
        InputStreamResource resource = new InputStreamResource(inputStream);

        // Return ResponseEntity
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}