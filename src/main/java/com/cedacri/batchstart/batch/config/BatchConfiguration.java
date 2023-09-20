package com.cedacri.batchstart.batch.config;

import com.cedacri.batchstart.batch.job.InvalidDataStep.InvalidDataReader;
import com.cedacri.batchstart.batch.job.InvalidDataStep.InvalidPersonRequestDto;
import com.cedacri.batchstart.batch.job.InvalidDataStep.StringHeaderWriter;
import com.cedacri.batchstart.batch.job.listener.JobCompletionNotificationListener;
import com.cedacri.batchstart.batch.job.processors.DtoToPersonItemProcessor;
import com.cedacri.batchstart.batch.job.processors.CustomPersonValidator;
import com.cedacri.batchstart.batch.job.tasklet.MoveFilesTasklet;
import com.cedacri.batchstart.batch.listeners.StepItemProcessListener;
import com.cedacri.batchstart.batch.listeners.StepItemReadListener;
import com.cedacri.batchstart.batch.listeners.StepItemWriteListener;
import com.cedacri.batchstart.model.Person;
import com.cedacri.batchstart.model.PersonRequestDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

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
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PersonRequestDto>() {{
                    setTargetType(PersonRequestDto.class);
                }})
                .build();
    }

    @Bean
    BeanValidatingItemProcessor<PersonRequestDto> validatingJSR303Processor() throws Exception {
        BeanValidatingItemProcessor<PersonRequestDto> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setFilter(false);//items that fail validation are filtered (null is returned and skip the invalid items occured).
        //setFilter(false); throws ValidationException, whenever it finds an item that do not meet validation criteria. It leads to entire job in FAILED state.
        itemProcessor.afterPropertiesSet();

        return itemProcessor;
    }

    //ValidatingItemProcessor is used to validate the input and return without any modification.
    // Using this processor either we can skip the item if it does not meet the validation criteria or fail the job.
    @Bean
    public ValidatingItemProcessor<PersonRequestDto> validatingCustomItemProcessor() {
        ValidatingItemProcessor<PersonRequestDto> itemProcessor = new ValidatingItemProcessor<>();
        itemProcessor.setValidator(new CustomPersonValidator());
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

//                .listener(new MySkipListener())
                .listener(new StepItemProcessListener())
                .listener(new StepItemWriteListener())
                .listener(new StepItemReadListener())

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
            PlatformTransactionManager transactionManager) {
        return new StepBuilder("writeIntoCsvInvalidDataStep", jobRepository)
                .<InvalidPersonRequestDto, InvalidPersonRequestDto> chunk(2, transactionManager)
                .reader(invalidDataReader())
                .writer(invalidDataWriter())
                .build();
    }

    @Bean
    public ItemReader<InvalidPersonRequestDto> invalidDataReader() {
        return new InvalidDataReader();
    }

    @Bean
    public ItemWriter<InvalidPersonRequestDto> invalidDataWriter() {
        String exportFileHeader = "firstName, lastName, occurredError";
        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);

        return new FlatFileItemWriterBuilder<InvalidPersonRequestDto>()
                .name("invalidDataWriter")
                .headerCallback(headerWriter)
                .lineAggregator( createPersonLineAggregator() )
                .resource( new FileSystemResource("src/main/resources/processed_data/invalid_data/invalid.csv") )
                .build();
    }

    private FieldExtractor<InvalidPersonRequestDto> createPersonFieldExtractor() {
        BeanWrapperFieldExtractor<InvalidPersonRequestDto> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {
                "firstName", "lastName", "occurredError"
        });
        return extractor;
    }

    private LineAggregator<InvalidPersonRequestDto> createPersonLineAggregator() {
        DelimitedLineAggregator<InvalidPersonRequestDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        FieldExtractor<InvalidPersonRequestDto> fieldExtractor = createPersonFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }
}
