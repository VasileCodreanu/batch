//package com.cedacri.batchstart.batch.job.tasklet;
//
//import com.cedacri.batchstart.batch.job.InvalidDataStep.InvalidDataReader;
//import com.cedacri.batchstart.batch.listeners.StepItemProcessListener;
//import com.cedacri.batchstart.model.PersonRequestDto;
//import org.springframework.batch.core.StepContribution;
//import org.springframework.batch.core.scope.context.ChunkContext;
//import org.springframework.batch.core.step.tasklet.Tasklet;
//import org.springframework.batch.item.ExecutionContext;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.file.FlatFileItemWriter;
//import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
//import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
//import org.springframework.batch.item.support.ListItemReader;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.FileSystemResource;
//
//import java.util.List;
//
//public class InvalidDataWriteTasklet implements Tasklet {
//
////    @Value("${output.file.path}")
//    private String outputPath = "\"src/main/resources/invalid.csv\"";
//
//    @Override
//    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//
//        FlatFileItemWriter<PersonRequestDto> writer = new FlatFileItemWriter<>();
//        writer.setResource(new FileSystemResource(outputPath));
//
//        DelimitedLineAggregator<PersonRequestDto> lineAggregator = new DelimitedLineAggregator<>();
//        BeanWrapperFieldExtractor<PersonRequestDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
//        fieldExtractor.setNames(new String[]{
//                "firstName",
//                "lastName"});
//        lineAggregator.setFieldExtractor(fieldExtractor);
//
//        writer.setLineAggregator(lineAggregator);
//        writer.open(new ExecutionContext());
//        writer.write(getDataToWrite());
//        writer.close();
//
//        return RepeatStatus.FINISHED;
//    }
//
//    private ItemReader<PersonRequestDto> getDataToWrite() {
//        return new InvalidDataReader();
//    }
//}
