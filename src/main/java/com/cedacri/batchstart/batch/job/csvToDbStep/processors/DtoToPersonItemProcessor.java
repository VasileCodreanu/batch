package com.cedacri.batchstart.batch.job.csvToDbStep.processors;

import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtoToPersonItemProcessor implements ItemProcessor<PersonRequestDto, Person> {

    private static final Logger log = LoggerFactory.getLogger(DtoToPersonItemProcessor.class);

    @Override
    public Person process(final PersonRequestDto personDto) {

        final String firstName = personDto.getFirstName().toUpperCase();
        final String lastName  = personDto.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("CONVERTING (" + personDto + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }
}
