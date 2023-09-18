package com.cedacri.batchstart.batch.listeners;

import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.SkipListener;

public class MySkipListener implements SkipListener<PersonRequestDto, Person> {
    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println("-----MySkipListener| On Skip in Read Error : " + t.getMessage());
//        SkipListener.super.onSkipInRead(t);
    }

    @Override
    public void onSkipInWrite(Person item, Throwable t) {
        System.out.println("-----MySkipListener | Skipped in Write due to : " + t.getMessage()+", Item ="+item);
//        SkipListener.super.onSkipInWrite(item, t);
    }

    @Override
    public void onSkipInProcess(PersonRequestDto item, Throwable t) {
        //e logic on what happens when an item is not validated (writtes to a file, a DB,
        System.out.println("-----------MySkipListener | Skipped in process due to: " + t.getMessage()+", Item ="+item);
//        SkipListener.super.onSkipInProcess(item, t);
    }
}
