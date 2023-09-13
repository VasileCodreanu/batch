package com.cedacri.demo;

import com.cedacri.demo.model.Person;
import com.cedacri.demo.model.PersonRequestDto;
import org.springframework.batch.item.ItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//business processing of an item

//ItemProcessor interface makes it easy to wire the code into a batch job,
// According to the interface, you receive an incoming Person object,
// after which you transform it to an upper-cased Person.
/////The input and output types need not be the same. In fact, after one source of data is read, sometimes the application’s data flow needs a different data type.
public class PersonItemProcessor  implements ItemProcessor<PersonRequestDto, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final PersonRequestDto personDto) throws Exception {
        final String firstName = personDto.getFirstName().toUpperCase();
        final String lastName  = personDto.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + personDto + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }
}
