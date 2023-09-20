package com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep;

import com.cedacri.batchstart.model.InvalidPersonRequestDto;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.stereotype.Component;

@Component
public class CustomLineAggregator {

    public LineAggregator<InvalidPersonRequestDto> createPersonLineAggregator() {
        DelimitedLineAggregator<InvalidPersonRequestDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        FieldExtractor<InvalidPersonRequestDto> fieldExtractor = createPersonFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<InvalidPersonRequestDto> createPersonFieldExtractor() {
        BeanWrapperFieldExtractor<InvalidPersonRequestDto> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {
                "firstName", "lastName", "occurredError"
        });
        return extractor;
    }
}
