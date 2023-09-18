package com.cedacri.batchstart.batch.config;

import com.cedacri.batchstart.batch.job.listener.JobCompletionNotificationListener;
import com.cedacri.batchstart.batch.job.processors.DtoToPersonItemProcessor;
import com.cedacri.batchstart.batch.job.processors.PersonValidator;
import com.cedacri.batchstart.batch.job.tasklet.MoveFilesTasklet;
import com.cedacri.batchstart.batch.listeners.*;
import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.Validator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
//@EnableTask annotation sets up a TaskRepository, which stores information about the task execution (such as the start and end times of the task and the exit code)
public class BatchConfiguration {
    @Value("${input.file.name}")
    private String inputFileName;

    @Bean
    public FlatFileItemReader<PersonRequestDto> reader() {
        return new FlatFileItemReaderBuilder<PersonRequestDto>()
                .name("personItemReader")
                .resource(new ClassPathResource(inputFileName))
                .delimited()//DelimitedLineTokenizer defaults to comma as its delimiter
                .names(new String[]{"firstName", "lastName"})
//                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PersonRequestDto>() {{
                    setTargetType(PersonRequestDto.class);
                }})
                .build();
    }

    @Bean
    BeanValidatingItemProcessor<PersonRequestDto> validatingJSR303Processor() throws Exception {
        BeanValidatingItemProcessor<PersonRequestDto> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setFilter(true);// to skip the invalid items ,  items that fail validation are filtered (null is returned).
        //setFilter(false); //throw ValidationException, whenever it finds an item that do not meet validation criteria. It leads to entire job in FAILED state.
        itemProcessor.afterPropertiesSet();

        return itemProcessor;
        //BeanValidatingItemProcessor<T> extends ValidatingItemProcessor<T>
        //A ValidatingItemProcessor that uses the Bean Validation API (JSR-303) to validate items.
    }


    //ValidatingItemProcessor is used to validate the input and return without any modification.
    // Using this processor either we can skip the item if it does not meet the validation criteria or fail the job.
    @Bean
    public ValidatingItemProcessor<PersonRequestDto> validatingCustomItemProcessor() {
        ValidatingItemProcessor<PersonRequestDto> itemProcessor = new ValidatingItemProcessor<>();
        itemProcessor.setValidator(new PersonValidator());
        itemProcessor.setFilter(false);

        return itemProcessor;
    }

    @Bean
    public DtoToPersonItemProcessor dtoToEntityProcessor() {
        return new DtoToPersonItemProcessor();
    }

    @Bean
    public CompositeItemProcessor compositeProcessor() throws Exception {
        List<ItemProcessor> delegates = new ArrayList<>(3);

        delegates.add(validatingJSR303Processor());
        delegates.add(validatingCustomItemProcessor());
        delegates.add(dtoToEntityProcessor());

        CompositeItemProcessor processor = new CompositeItemProcessor();
        processor.setDelegates(delegates);
        processor.afterPropertiesSet();
        return processor;
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    // Jobs are built from steps, where each step can involve a reader, a processor, and a writer.
    //A job needs to be launched (with JobLauncher), and metadata about the currently running process needs to be stored (in JobRepository).
    @Bean
    public Job importPersonJob(JobRepository jobRepository,
                             JobCompletionNotificationListener listener,
                             Step csvToDbStep, Step moveFilesStep) {
        return new JobBuilder("importPersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(csvToDbStep)
                .next(moveFilesStep)
                .end()
                .build();
    }

    @Bean
    public Step csvToDbStep(JobRepository jobRepository,//Job repositories are abstractions responsible for storing and updating metadata information related to Job instance executions and Job contexts.
                            PlatformTransactionManager transactionManager,
                            JdbcBatchItemWriter<Person> writer) throws Exception {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<PersonRequestDto, Person> chunk(2, transactionManager)
                // generic method. This represents the input and output types of each “chunk” of processing and lines up with ItemReader<Person> and ItemWriter<Person>.
                .reader(reader())
                .processor(compositeProcessor())
                .writer(writer)

                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(12)

                .listener(new MySkipListener())
                .listener(new StepItemProcessListener())
                .listener(new StepItemWriteListener())
                .listener(new StepItemReadListener())
                .listener(new StepChunkListener())

                .build();
    }

    @Bean
    public Step moveFilesStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MoveFilesTasklet moveFilesTasklet) {
        return new StepBuilder("moveFilesStep", jobRepository)
                .tasklet(moveFilesTasklet, transactionManager)
                .listener(new MyStepListener())
                .build();
    }
}
