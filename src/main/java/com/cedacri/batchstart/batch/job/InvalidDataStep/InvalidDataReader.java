package com.cedacri.batchstart.batch.job.InvalidDataStep;

import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;

public class InvalidDataReader implements ItemReader<PersonRequestDto> {

    private int nextIndex;
    List<PersonRequestDto> invalidData;

    public void setInvalidData(PersonRequestDto invalidItem) {
        this.invalidData.add(invalidItem);
    }

    public InvalidDataReader() {
        invalidData = new ArrayList<>();
        this.nextIndex = 0;
    }

    @Override
    public PersonRequestDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        PersonRequestDto next = null;

        if (nextIndex < invalidData.size()) {
            next = invalidData.get(nextIndex++);
        }
        else{
            nextIndex = 0;
        }
        return next;
    }
}
