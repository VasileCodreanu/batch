package com.cedacri.demo;

import com.cedacri.demo.model.PersonRequestDto;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

public class PersonValidator implements Validator<PersonRequestDto> {

    @Override
    public void validate(PersonRequestDto dto) throws ValidationException {
        if (dto.getFirstName() == null) {
            throw new ValidationException("firstName must not be null");
        }

        if (dto.getLastName() == null) {
            throw new ValidationException("lastName must not be null");
        }
    }


}
