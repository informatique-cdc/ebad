package fr.icdc.ebad.web.rest.validator;

import fr.icdc.ebad.web.rest.dto.CreationAccreditationRequestDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccreditationRequestValidatorTest {

    private final AccreditationRequestValidator accreditationRequestValidator = new AccreditationRequestValidator();

    @Test
    void isValidFalse() {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto
                .builder()
                .wantManage(false)
                .wantUse(false)
                .build();
        boolean result = accreditationRequestValidator.isValid(creationAccreditationRequestDto, null);
        assertFalse(result);
    }

    @Test
    void isValidTrue1() {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto
                .builder()
                .wantManage(true)
                .wantUse(false)
                .build();
        boolean result = accreditationRequestValidator.isValid(creationAccreditationRequestDto, null);
        assertTrue(result);
    }

    @Test
    void isValidTrue2() {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto
                .builder()
                .wantManage(false)
                .wantUse(true)
                .build();
        boolean result = accreditationRequestValidator.isValid(creationAccreditationRequestDto, null);
        assertTrue(result);
    }

    @Test
    void isValidTrue3() {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto
                .builder()
                .wantManage(true)
                .wantUse(true)
                .build();
        boolean result = accreditationRequestValidator.isValid(creationAccreditationRequestDto, null);
        assertTrue(result);
    }
}