package com.cedacri.batchstart.batch.listeners;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

public class StepChunkListener implements ChunkListener {
    @Override
    public void afterChunkError(ChunkContext context) {
        System.out.println("-----CHUNK error");
        ChunkListener.super.afterChunkError(context);
    }
}
