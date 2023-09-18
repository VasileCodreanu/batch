package com.cedacri.batchstart.batch.listeners;

import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.ItemReadListener;

public class StepItemReadListener  implements ItemReadListener<PersonRequestDto> {
    @Override
    public void beforeRead() {
        System.out.println("ItemReadListener - beforeRead");
        ItemReadListener.super.beforeRead();
    }

    @Override
    public void afterRead(PersonRequestDto item) {
        System.out.println("ItemReadListener - afterRead");
        ItemReadListener.super.afterRead(item);
    }

    @Override
    public void onReadError(Exception ex) {
        System.out.println("------------ --- ItemReadListener - onReadError");
        ItemReadListener.super.onReadError(ex);
    }
}
