package ws.billdavis.services.validation.address;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

@Controller
public class AddressValidationService {

    @RequestMapping
    public @ResponseBody Set<ConstraintViolation> validatePostalCode(
        @RequestParam(value="countryCode") String countryCode,
        @RequestParam(value="postalCode") String postalCode ) {

        return new HashSet<>();
    }
}
