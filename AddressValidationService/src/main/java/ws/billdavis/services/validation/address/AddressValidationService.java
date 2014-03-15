package ws.billdavis.services.validation.address;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

@Controller
public class AddressValidationService {

    @RequestMapping( "/addressValidation" )
    public @ResponseBody Set<ConstraintViolation> validatePostalCode(
        @RequestParam(value="postalCode")PostalCodeText postalCode ) {

        return new HashSet<>();
    }

    @RequestMapping( "/test" )
    public @ResponseBody PostalCodeText test() {
        PostalCodeText output = new PostalCodeText();
        output.setCountryCode( "US" );
        output.setPostalCode( "61704" );
        return output;
    }
}
