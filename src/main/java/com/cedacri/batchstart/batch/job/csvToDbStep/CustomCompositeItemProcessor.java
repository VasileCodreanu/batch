package com.cedacri.batchstart.batch.job.csvToDbStep;

import com.cedacri.batchstart.batch.job.csvToDbStep.processors.CustomPersonValidator;
import com.cedacri.batchstart.batch.job.csvToDbStep.processors.DtoToPersonItemProcessor;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomCompositeItemProcessor{
    List<ItemProcessor> delegates;
    CustomCompositeItemProcessor(){
        delegates = new ArrayList<>(3);
    }

    public CompositeItemProcessor compositeProcessor() throws Exception {

        delegates.add(validatingJSR303Processor());
        delegates.add(validatingCustomItemProcessor());
        delegates.add(dtoToEntityProcessor());

        CompositeItemProcessor processor = new CompositeItemProcessor();
        processor.setDelegates(delegates);
        processor.afterPropertiesSet();
        return processor;
    }

    private BeanValidatingItemProcessor<PersonRequestDto> validatingJSR303Processor() throws Exception {
        BeanValidatingItemProcessor<PersonRequestDto> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setFilter(false);//items that fail validation are filtered (null is returned and skip the invalid items occured).
        //setFilter(false); throws ValidationException, whenever it finds an item that do not meet validation criteria. It leads to entire job in FAILED state.
        itemProcessor.afterPropertiesSet();

        return itemProcessor;
    }

    //ValidatingItemProcessor is used to validate the input and return without any modification.
    // Using this processor either we can skip the item if it does not meet the validation criteria or fail the job.
    private ValidatingItemProcessor<PersonRequestDto> validatingCustomItemProcessor() {
        ValidatingItemProcessor<PersonRequestDto> itemProcessor = new ValidatingItemProcessor<>();
        itemProcessor.setValidator(new CustomPersonValidator());
        itemProcessor.setFilter(false);

        return itemProcessor;
    }

    private DtoToPersonItemProcessor dtoToEntityProcessor() {
        return new DtoToPersonItemProcessor();
    }
}
