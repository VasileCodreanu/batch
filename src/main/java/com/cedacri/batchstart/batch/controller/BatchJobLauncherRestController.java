//package com.cedacri.batchstart.batch.job;
//
//@RestController
//@RequestMapping("/load")
//public class BatchJobLauncherRestController {
//
//    JobLauncher jobLauncher;
//
//    @Autowired
//    Job simpleJob;
//
//    @RequestMapping("/launch/welcome/job")
//    public String jobLauncher() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
//
//        Logger logger = LoggerFactory.getLogger(this.getClass());
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("time", System.currentTimeMillis())
//                    .toJobParameters();
//            //job launcher is an interface for running the jobs
//              JobExecution jobExecution = jobLauncher.run(simpleJob, jobParameters);

//                 System.out.println("JobExecution: " + jobExecution.getStatus());
//
//                         System.out.println("Batch is Running...");
//                         while (jobExecution.isRunning()) {
//                              System.out.println("...");
//                         }

//        } catch (Exception e) {
//            logger.info(e.getMessage());
//        }
//
//         return jobExecution.getStatus();
//    }
//}
