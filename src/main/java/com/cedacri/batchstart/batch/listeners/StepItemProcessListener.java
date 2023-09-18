package com.cedacri.batchstart.batch.listeners;

import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.ItemProcessListener;

public class StepItemProcessListener implements ItemProcessListener<PersonRequestDto, Person> {
    @Override
    public void beforeProcess(PersonRequestDto item) {
        System.out.println("ItemProcessListener - beforeProcess");
    }

    @Override
    public void afterProcess(PersonRequestDto item, Person result) {
        System.out.println("ItemProcessListener - afterProcess");
    }

    @Override
    public void onProcessError(PersonRequestDto item, Exception e) {
        //add this not valida data to customecsv out[ut | new database table
        System.out.println(" - ----------StepItemProcessListener - ----- onProcessError--"+ e.getMessage());
    }
}
