package com.cedacri.batchstart.batch.listeners;

import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.SkipListener;

import java.util.LinkedList;
import java.util.List;

public class MySkipListener implements SkipListener<PersonRequestDto, Person> {

    private final List<PersonRequestDto> invalidData = new LinkedList<>();

    public List<PersonRequestDto> getInvalidData() {
        return invalidData;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println("-----MySkipListener| On Skip in Read Error : " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(Person item, Throwable t) {
        System.out.println("-----MySkipListener | Skipped in Write due to : " + t.getMessage()+", Item ="+item);
    }

    @Override
    public void onSkipInProcess(PersonRequestDto item, Throwable t) {
        //e logic on what happens when an item is not validated (writtes to a file, a DB,
        invalidData.add(item);

        System.out.println("-----------MySkipListener | Skipped in process due to: " + t.getMessage()+", Item ="+item);
    }
}
