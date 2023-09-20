package com.cedacri.batchstart.batch.job.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class MoveFilesTasklet implements Tasklet {

    @Value("${input.file.name}")
    private String inputFile;

    private static final Logger log = LoggerFactory.getLogger(MoveFilesTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String FILE_TO_BE_MOVED_PATH = "src/main/resources/sample-data.csv";
        String DESTINATION_DIRECTORY_PATH = "src/main/resources/processed_data";

        Path sourceFilePath = Path.of(FILE_TO_BE_MOVED_PATH);
        Path destinationDirectory = Path.of(DESTINATION_DIRECTORY_PATH);

        try {
            if (!Files.exists(destinationDirectory))  Files.createDirectories(destinationDirectory);

            Path destinationPath = destinationDirectory.resolve(sourceFilePath.getFileName());

            Files.move(sourceFilePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            log.warn("!!! - - .csv File was moved after processing");
        } catch (FileAlreadyExistsException e) {
            log.error("!!! - - File already exists in the destination directory.");
        } catch (IOException e) {
            log.error("!!! - - An error occurred: " + e.getMessage());
        }
        return RepeatStatus.FINISHED;
    }
}
