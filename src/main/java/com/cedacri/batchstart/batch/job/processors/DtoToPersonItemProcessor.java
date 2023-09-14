package com.cedacri.batchstart.batch.job.processors;

import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class DtoToPersonItemProcessor implements ItemProcessor<PersonRequestDto, Person> {

    private static final Logger log = LoggerFactory.getLogger(DtoToPersonItemProcessor.class);
    private static final List<String> NAMES_TO_BE_EXCLUDED = Arrays.asList("Tom", "Jerry", "Justin");

    @Override
    public Person process(final PersonRequestDto personDto) {

        if (    NAMES_TO_BE_EXCLUDED.contains(personDto.getFirstName()) ||
                NAMES_TO_BE_EXCLUDED.contains(personDto.getLastName())) {
            log.info("NAMES_TO_BE_EXCLUDED found (" + personDto + ')' + " DtoToPersonItemProcessor.class");
            return null;//to be skipped
        }

        final String firstName = personDto.getFirstName().toUpperCase();
        final String lastName  = personDto.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("CONVERTING (" + personDto + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }
}
