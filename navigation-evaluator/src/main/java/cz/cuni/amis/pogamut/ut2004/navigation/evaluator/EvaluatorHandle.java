package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.utils.process.ProcessExecution;
import cz.cuni.amis.utils.process.ProcessExecutionConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Handle for {@link SingleTaskEvaluator}. Runs {@link SingleTaskEvaluator} in
 * new JVM.
 *
 * @author Bogo
 */
public class EvaluatorHandle {

    private String statusFilePath;
    private Object evaluatorJVMHandle;
    private Status status;
    private Date timeStamp;
    private ProcessExecution processExecution;
    private File task;
    //Allows debugging of the newly created JVM
    private boolean isDebug = false;

    public File getTask() {
        return task;
    }

    public EvaluatorHandle() {
        statusFilePath = String.format("statusFile-%d", new Date().getTime());
        status = Status.NEW;
    }

    /**
     * Creates new JVM and starts {@link SingleTaskEvaluator} in it.
     *
     * @param task
     * @return
     */
    public boolean createEvaluator(File task, Logger log) {
        this.task = task;
        if (status != Status.NEW) {
            return false;
        }

        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ArrayList<String> command = new ArrayList<String>();
        //command.add(path);
        command.add("-cp");
        command.add(classpath);

        //Debugging options
        if (isDebug) {
            command.add("-Xdebug");
            command.add("-Xrunjdwp:transport=dt_socket,server=y,address=8888,suspend=y");
        }

        command.add(SingleTaskEvaluator.class.getName());
        command.add(task.getAbsolutePath());

        ProcessExecutionConfig config = new ProcessExecutionConfig();
        config.setPathToProgram(path);
        config.setExecutionDir(".");
        config.setArgs(command);
        config.setRedirectStdErr(false);
        config.setRedirectStdOut(false);
        processExecution = new ProcessExecution(config, log);
        //ProcessBuilder processBuilder = new ProcessBuilder(command);

        processExecution.start();



        status = Status.CREATED;
        return true;
    }

    public Status getStatus() {
        if (processExecution != null) {
            //try {
            if (processExecution.isRunning()) {
                return Status.RUNNING;
            }
            switch (processExecution.getExitValue()) {
                case 0:
                    status = Status.COMPLETED;
                    processExecution = null;
                    break;
                case -1:
                case -2:
                case -3:
                    status = Status.FAILED;
                    break;
            }
        }

//    catch(IllegalThreadStateException ex
//
//    
//        ) {
//                status = Status.RUNNING;
//    }
//}
        return status;


    }

    /**
     * Status of handle.
     */
    public enum Status {

        NEW, CREATED, RUNNING, NOT_RESPONDING, FAILED, DESTROYED, COMPLETED
    }
}
