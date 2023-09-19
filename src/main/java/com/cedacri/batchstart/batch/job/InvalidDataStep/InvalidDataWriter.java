package com.cedacri.batchstart.batch.job.InvalidDataStep;

import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.item.file.FlatFileItemWriter;

public class InvalidDataWriter extends FlatFileItemWriter<PersonRequestDto> {
}
