package com.cedacri.batchstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//When using Java configuration, the @EnableBatchProcessing annotation provides a JobRepository as one of the components that is automatically configured.
public class BatchApplication {

	//Although batch processing can be embedded in web apps and WAR files, the simpler approach demonstrated below creates a standalone application.
	// You package everything in a single, executable JAR file, driven by a good old Java main() method.

	public static void main(String[] args) {
		// SpringApplication.exit() and System.exit() ensure that the JVM exits upon job completion
		System.exit(SpringApplication.exit(SpringApplication.run(BatchApplication.class, args)));
	}

}
