package com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep;

import com.cedacri.batchstart.model.InvalidPersonRequestDto;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InvalidDataReader implements ItemReader<InvalidPersonRequestDto> {

    private int nextIndex;
    static List<InvalidPersonRequestDto> invalidData;

    public void setInvalidData(PersonRequestDto invalidItem, Exception error) {
        this.invalidData.add(new InvalidPersonRequestDto(invalidItem.getFirstName(), invalidItem.getLastName(), error.getMessage().trim()));
    }

    public InvalidDataReader() {
        invalidData = new ArrayList<>();
        this.nextIndex = 0;
    }

    @Override
    public InvalidPersonRequestDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        InvalidPersonRequestDto next = null;

        if (nextIndex < invalidData.size()) {
            next = invalidData.get(nextIndex++);
        }else{
            nextIndex = 0;
        }
        return next;
    }
}
