package fr.icdc.ebad.web.rest.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AccreditationRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccreditationValidator {
    String message() default "{error.accreditation}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}