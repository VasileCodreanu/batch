package com.cedacri.batchstart.batch.job.listeners;

import com.cedacri.batchstart.model.Person;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

public class StepItemWriteListener implements ItemWriteListener<Person> {

    @Override
    public void beforeWrite(Chunk<? extends Person> items) {
        System.out.println("ItemWriteListener - beforeWrite");
        ItemWriteListener.super.beforeWrite(items);
    }

    @Override
    public void afterWrite(Chunk<? extends Person> items) {
        System.out.println("ItemWriteListener - afterWrite");
        ItemWriteListener.super.afterWrite(items);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Person> items) {
        System.out.println("------------ ---ItemWriteListener - onWriteError");
        ItemWriteListener.super.onWriteError(exception, items);
    }
}
