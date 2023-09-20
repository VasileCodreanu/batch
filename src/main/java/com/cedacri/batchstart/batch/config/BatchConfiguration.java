package com.cedacri.batchstart.batch.config;

import com.cedacri.batchstart.batch.job.csvToDbStep.CsvToDbStepFlatFileItemReader;
import com.cedacri.batchstart.batch.job.csvToDbStep.CsvToDbStepJdbcBatchItemWriter;
import com.cedacri.batchstart.batch.job.csvToDbStep.CustomCompositeItemProcessor;
import com.cedacri.batchstart.batch.job.listeners.JobCompletionNotificationListener;
import com.cedacri.batchstart.batch.job.listeners.StepItemProcessListener;
import com.cedacri.batchstart.batch.job.moveFileAfterProcessStep.MoveFilesTasklet;
import com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep.CustomLineAggregator;
import com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep.InvalidDataReader;
import com.cedacri.batchstart.batch.job.writeIntoCsvInvalidDataStep.InvalidDataWriter;
import com.cedacri.batchstart.model.InvalidPersonRequestDto;
import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
//@EnableTask annotation sets up a TaskRepository, which stores information about the task execution (such as the start and end times of the task and the exit code)
public class BatchConfiguration {

    //A job needs to be launched (with JobLauncher), and metadata about the currently running process needs to be stored (in JobRepository).
    @Bean
    public Job importPersonJob( JobRepository jobRepository,
                                JobCompletionNotificationListener jobCompletionListener,
                                Step csvToDbStep,
                                Step moveFilesAfterProcessStep,
                                Step writeIntoCsvInvalidDataStep) {
        return new JobBuilder("importPersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionListener)
                .flow(csvToDbStep)
                .next(moveFilesAfterProcessStep)
                .next(writeIntoCsvInvalidDataStep)
                .end()
                .build();
    }

    @Bean
    public Step csvToDbStep(JobRepository jobRepository,//abstractions responsible for storing and updating metadata information related to Job instance executions and Job contexts.
                            PlatformTransactionManager transactionManager,
                            CustomCompositeItemProcessor compositeItemProcessor,
                            CsvToDbStepFlatFileItemReader flatFileItemReader,
                            CsvToDbStepJdbcBatchItemWriter jdbcBatchItemWriter,
                            DataSource dataSource) throws Exception {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<PersonRequestDto, Person> chunk(2, transactionManager)
                // generic method. This represents the input and output types of each “chunk” of processing and lines up with ItemReader<PersonRequestDto> and ItemWriter<Person>.
                .reader(flatFileItemReader.reader())
                .processor(compositeItemProcessor.compositeProcessor())
                .writer(jdbcBatchItemWriter.writer(dataSource))

                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(999)

                .listener(new StepItemProcessListener())

                .build();
    }

    @Bean
    public Step moveFilesAfterProcessStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MoveFilesTasklet moveFilesTasklet) {
        return new StepBuilder("moveFilesAfterProcessStep", jobRepository)
                .tasklet(moveFilesTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step writeIntoCsvInvalidDataStep(
            JobRepository jobRepository,
            InvalidDataWriter invalidDataWriter,
            InvalidDataReader invalidDataReader,
            CustomLineAggregator customLineAggregator,
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("writeIntoCsvInvalidDataStep", jobRepository)
                .<InvalidPersonRequestDto, InvalidPersonRequestDto> chunk(2, transactionManager)
                .reader(invalidDataReader)
                .writer(invalidDataWriter.invalidDataWriter(customLineAggregator))
                .build();
    }
}
