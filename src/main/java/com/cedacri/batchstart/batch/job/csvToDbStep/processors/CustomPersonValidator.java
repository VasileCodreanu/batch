package com.cedacri.batchstart.batch.job.csvToDbStep.processors;

import com.cedacri.batchstart.model.PersonRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.Arrays;
import java.util.List;

public class CustomPersonValidator implements Validator<PersonRequestDto> {
    private static final Logger log = LoggerFactory.getLogger(CustomPersonValidator.class);
    private static final List<String> NAMES_TO_BE_EXCLUDED = Arrays.asList("Tom", "Jerry", "Jane");

    @Override
    public void validate(PersonRequestDto dto) throws ValidationException {
        if (    NAMES_TO_BE_EXCLUDED.contains(dto.getFirstName()) ||
                NAMES_TO_BE_EXCLUDED.contains(dto.getLastName())) {
            log.warn("!!! - - NAMES_TO_BE_EXCLUDED found (" + dto + ')' + " PersonValidator.class");
            throw new ValidationException("Validation failed for "+ dto+ ": Field error: firstName or lastName should not be equal 'Jane'");
        }
    }
}
