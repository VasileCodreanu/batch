package com.cedacri.batchstart.batch.job.csvToDbStep;

import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class CsvToDbStepFlatFileItemReader {

    @Value("${input.file.name}")
    private String inputFileName;

    public FlatFileItemReader<PersonRequestDto> reader() {
        return new FlatFileItemReaderBuilder<PersonRequestDto>()
                .name("personItemReader")
                .resource(new ClassPathResource(inputFileName))
                .delimited()//DelimitedLineTokenizer defaults to comma as its delimiter
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PersonRequestDto>() {{
                    setTargetType(PersonRequestDto.class);
                }})
                .build();
    }
}
