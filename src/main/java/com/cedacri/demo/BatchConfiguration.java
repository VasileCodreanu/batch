package com.cedacri.demo;

import com.cedacri.demo.model.Person;
import com.cedacri.demo.model.PersonRequestDto;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//To configure your job, you must first create a Spring @Configuration
@Configuration
public class BatchConfiguration {
    // ItemReader
    @Bean
    public FlatFileItemReader<PersonRequestDto> reader() {
        return new FlatFileItemReaderBuilder<PersonRequestDto>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()////DelimitedLineTokenizer defaults to comma as its delimiter
                .names(new String[]{"firstName", "lastName"})
//                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<PersonRequestDto>() {{
                    setTargetType(PersonRequestDto.class);
                }})
                .build();
    }

    @Bean
    ValidatingItemProcessor<PersonRequestDto> validatingItemProcessor(){
//        allows you to validate items annotated with the Bean Validation API (JSR-303) annotations
        BeanValidatingItemProcessor<PersonRequestDto> itemProcessor = new BeanValidatingItemProcessor<>();
        itemProcessor.setValidator(new PersonValidator());
        itemProcessor.setFilter(true);
        return itemProcessor;
    }

    //processor
    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }
    // creates an ItemWriter
    //This one is aimed at a JDBC destination and automatically gets a copy of the dataSource created by @EnableBatchProcessing.
    // It includes the SQL statement needed to insert a single Person, driven by Java bean properties.
    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    //next is actual job configuration:
    // The first method defines the job, and the second one defines a single step.
    // Jobs are built from steps, where each step can involve a reader, a processor,
    // and a writer.
    //A job needs to be launched (with JobLauncher), and metadata about the currently running process needs to be stored (in JobRepository).
    @Bean
    public Job importUserJob(JobRepository jobRepository,
                             JobCompletionNotificationListener listener,
                             Step csvToDb) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(csvToDb)
//                .next(step2)//for multiple steps
                .end()
                .build();
    }

    //actual job configuration
    @Bean
    public Step csvToDb(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      JdbcBatchItemWriter<Person> writer) {
        return new StepBuilder("csvToDb", jobRepository)
                .<PersonRequestDto, Person> chunk(2, transactionManager)
                // generic method. This represents the input and output types of each “chunk” of processing and lines up with ItemReader<Person> and ItemWriter<Person>.
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
