package com.cedacri.batchstart.batch.config;

import com.cedacri.batchstart.batch.job.JobCompletionNotificationListener;
import com.cedacri.batchstart.batch.job.processors.DtoToPersonItemProcessor;
import com.cedacri.batchstart.batch.job.processors.PersonValidator;
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
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

//To configure your job, you must first create a Spring @Configuration
@Configuration
public class BatchConfiguration {

    @Bean
    public FlatFileItemReader<PersonRequestDto> reader() {
        return new FlatFileItemReaderBuilder<PersonRequestDto>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()//DelimitedLineTokenizer defaults to comma as its delimiter
                .names(new String[]{"firstName", "lastName"})
//                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PersonRequestDto>() {{
                    setTargetType(PersonRequestDto.class);
                }})
                .build();
    }

    //validate items annotated with the Bean Validation API (JSR-303) annotations
    @Bean
    BeanValidatingItemProcessor<PersonRequestDto> validatingJSR303Processor() {
        BeanValidatingItemProcessor<PersonRequestDto> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setFilter(true);// to skip the invalid items
        //setFilter(false); //throw ValidationException, whenever it finds an item that do not meet validation criteria. It leads to entire job in FAILED state.
        return itemProcessor;
    }


    //ValidatingItemProcessor is used to validate the input and return without any modification.
    // Using this processor either we can skip the item if it does not meet the validation criteria or fail the job.
    @Bean
    public ValidatingItemProcessor<PersonRequestDto> validatingCustomItemProcessor() {
        ValidatingItemProcessor<PersonRequestDto> itemProcessor = new ValidatingItemProcessor<>(new PersonValidator());

        itemProcessor.setFilter(true);
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
                             Step csvToDbStep) {
        return new JobBuilder("importPersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(csvToDbStep)
                .end()
                .build();
    }

    @Bean
    public Step csvToDbStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            JdbcBatchItemWriter<Person> writer) throws Exception {
        return new StepBuilder("csvToDb", jobRepository)
                .<PersonRequestDto, Person> chunk(2, transactionManager)
                // generic method. This represents the input and output types of each “chunk” of processing and lines up with ItemReader<Person> and ItemWriter<Person>.
                .reader(reader())
                .processor(compositeProcessor())
                .writer(writer)
                .build();
    }
}
