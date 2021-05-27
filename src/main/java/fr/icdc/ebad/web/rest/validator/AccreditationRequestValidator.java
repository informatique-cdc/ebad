package fr.icdc.ebad.web.rest.validator;

import fr.icdc.ebad.web.rest.dto.CreationAccreditationRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AccreditationRequestValidator implements ConstraintValidator<AccreditationValidator, CreationAccreditationRequestDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccreditationRequestValidator.class);

    @Override
    public void initialize(AccreditationValidator constraintAnnotation) {
        LOGGER.debug("initialize AccreditationRequestValidator");
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(CreationAccreditationRequestDto creationAccreditationRequestDto, ConstraintValidatorContext constraintValidatorContext) {
        LOGGER.debug("isValid AccreditationRequestValidator");
        return creationAccreditationRequestDto.isWantManage() || creationAccreditationRequestDto.isWantUse();
    }
}
