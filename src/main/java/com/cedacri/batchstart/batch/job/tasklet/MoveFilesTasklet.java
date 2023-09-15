package com.cedacri.batchstart.batch.job.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class MoveFilesTasklet implements Tasklet {

    @Value("${input.file.name}")
    private String inputFile;

    private final String INITIAL_FILE_NAME = "sample-data.csv";
    private final String TARGET_FILE_NAME = "sample-data.csv";
    private final String DIRECTORY_NAME = "moved";
    private String MAIN_PATH = "src\\main\\resources";
    private static final Logger log = LoggerFactory.getLogger(MoveFilesTasklet.class);

    private final String FILE_TO_BE_MOVED_PATH = MAIN_PATH + "\\" +INITIAL_FILE_NAME;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Path directory_path = Files.createDirectory(Paths.get(MAIN_PATH + "\\" + DIRECTORY_NAME));

        File movedFile = new File( directory_path +"\\"+ TARGET_FILE_NAME);
        boolean fileCreated = movedFile.createNewFile();

        Files.move(Paths.get(FILE_TO_BE_MOVED_PATH), movedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        log.warn("!!! - - .csv File was moved after processing");

        return RepeatStatus.FINISHED;
    }
}
