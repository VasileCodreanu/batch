package com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep;

import com.cedacri.batchstart.model.InvalidPersonRequestDto;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class InvalidDataWriter  {

    public ItemWriter<InvalidPersonRequestDto> invalidDataWriter(
            CustomLineAggregator customLineAggregator) {
        String exportFileHeader = "firstName, lastName, occurredError";
        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);

        return new FlatFileItemWriterBuilder<InvalidPersonRequestDto>()
                .name("invalidDataWriter")
                .headerCallback(headerWriter)
                .lineAggregator( customLineAggregator.createPersonLineAggregator() )
                .resource( new FileSystemResource("src/main/resources/processed_data/invalid_data/invalid.csv") )
                .build();
    }
}
