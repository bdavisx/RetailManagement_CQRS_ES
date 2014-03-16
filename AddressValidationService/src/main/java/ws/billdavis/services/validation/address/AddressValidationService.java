package ws.billdavis.services.validation.address;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping( "/addressValidation")
public class AddressValidationService {

    @RequestMapping( method = RequestMethod.POST)
    public @ResponseBody List<ValidationConstraintError> validatePostalCode( @RequestBody PostalCodeText postalCode ) {
        final ArrayList<ValidationConstraintError> validationConstraintErrors = new ArrayList<>();
        validationConstraintErrors.add( new ValidationConstraintError() );
        return validationConstraintErrors;
    }

    @RequestMapping( method = RequestMethod.GET)
    public @ResponseBody PostalCodeText test() {
        PostalCodeText output = new PostalCodeText();
        output.setCountryCode( "US" );
        output.setPostalCode( "61704" );
        return output;
    }
}
