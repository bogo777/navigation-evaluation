package cz.cuni.amis.pogamut.ut2004.navigation.evaluator;

import cz.cuni.amis.pogamut.ut2004.navigation.evaluator.data.EvaluationTask;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Bogo
 */
public class EvaluatorHandle {
    
    private String statusFilePath;
    
    private Object evaluatorJVMHandle;
    
    private Status status;
    
    private Date timeStamp;
    
    private Process process;
    
    private EvaluationTask task;
    
    //Allows debugging of the newly created JVM
    private boolean isDebug = false;

    public EvaluationTask getTask() {
        return task;
    }
    
    public EvaluatorHandle() {
        statusFilePath = String.format("statusFile-%d", new Date().getTime());
        status = Status.NEW;
    }
    
    public boolean createEvaluator(EvaluationTask task) {
        if(status != Status.NEW) {
            return false;
        }
        
        String separator = System.getProperty("file.separator");
	String classpath = System.getProperty("java.class.path");
	String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ArrayList<String> command = new ArrayList<String>();
        command.add(path);
        command.add("-cp");
        command.add(classpath);
        
        //Debugging options
        if(isDebug) {
            command.add("-Xdebug");
            command.add("-Xrunjdwp:transport=dt_socket,server=y,address=8888,suspend=y");
        }
        
        command.add(SingleTaskEvaluator.class.getName());
        task.toArgs(command);
	ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            process = processBuilder.start();
        } catch (IOException ex) {
            status = Status.FAILED;
            return false;
        }
	
        
        status = Status.CREATED;
        return true;
    }
    
    public Status getStatus() {
        if(process != null) {
            try {
                switch(process.exitValue()) {
                    case 0:
                        status = Status.COMPLETED;
                        process = null;
                        break;
                    case -1:
                    case -2:
                    case -3:
                        status = Status.FAILED;
                        break;
                }
            } catch(IllegalThreadStateException ex) {
                status = Status.RUNNING;
            }
        }
        return status;
    }
    
    
    public enum Status {
        NEW, CREATED, RUNNING, NOT_RESPONDING, FAILED, DESTROYED, COMPLETED
    }
    
}