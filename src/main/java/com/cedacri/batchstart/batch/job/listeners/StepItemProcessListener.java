package com.cedacri.batchstart.batch.job.listeners;

import com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep.InvalidDataReader;
import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class StepItemProcessListener implements ItemProcessListener<PersonRequestDto, Person> {

    InvalidDataReader invalidDataReader;

    public StepItemProcessListener() {
        this.invalidDataReader = new InvalidDataReader();
    }

    @Override
    public void beforeProcess(PersonRequestDto item) {
        System.out.println("ItemProcessListener - beforeProcess");
    }

    @Override
    public void afterProcess(PersonRequestDto item, Person result) {
        System.out.println("ItemProcessListener - afterProcess");
    }

    @Override
    public void onProcessError(PersonRequestDto item, Exception error) {
        invalidDataReader.setInvalidData(item, error);
        System.out.println(" - ----------StepItemProcessListener - ----- onProcessError--"+ error.getMessage());
    }
}
