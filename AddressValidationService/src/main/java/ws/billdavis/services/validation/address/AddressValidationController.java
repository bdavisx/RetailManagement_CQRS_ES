package ws.billdavis.services.validation.address;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.nio.cs.ext.PCK;
import ws.billdavis.services.validation.ValidationConstraintError;
import ws.billdavis.services.validation.address.transferobjects.PostalCodeText;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping( "/addressValidation")
public class AddressValidationController {
    @Autowired
    private AddressValidationService addressValidationService;

    @RequestMapping( method = RequestMethod.POST)
    public @ResponseBody List<ValidationConstraintError> validatePostalCode( @RequestBody PostalCodeText postalCode ) {
        return addressValidationService.validatePostalCode( postalCode );
    }

    @RequestMapping( method = RequestMethod.GET)
    public @ResponseBody List<PostalCodeText> test() {
        PostalCodeText postalCodeText = new PostalCodeText();
        postalCodeText.setCountryCode( "US" );
        postalCodeText.setPostalCode( "61704" );
        List<PostalCodeText> postalCodeTexts = new ArrayList<>();
        postalCodeTexts.add( postalCodeText );
        return postalCodeTexts;
    }
}
