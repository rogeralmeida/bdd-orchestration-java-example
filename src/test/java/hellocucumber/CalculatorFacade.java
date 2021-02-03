package hellocucumber;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.*;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CalculatorFacade {
    private AWSStepFunctions sfnClient;
    private StateMachineListItem stateMachine;

    public CalculatorFacade() {
        ProfileCredentialsProvider profileCredentialsProvider = new ProfileCredentialsProvider("devaxacademy");
        try {
            profileCredentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles " +
                            "file. Please make sure that your credentials file is " +
                            "at the correct location (~/.aws/credentials), and is " +
                            "in valid format.", e);
        }
        Regions region = Regions.AP_SOUTHEAST_2;

        this.sfnClient = AWSStepFunctionsClientBuilder.standard()
                .withCredentials(profileCredentialsProvider)
                .withRegion(region)
                .build();

        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon Step Functions");
        System.out.println("===========================================\n");

        try {
            System.out.println("Listing state machines");
            List<StateMachineListItem> stateMachines = getStateMachineListItems();

            System.out.println("State machines count: " + stateMachines.size());
            if (!stateMachines.isEmpty()) {
                stateMachines.forEach(sm -> {
                    System.out.println("\t- Name: " + sm.getName());
                    System.out.println("\t- Arn: " + sm.getStateMachineArn());

                    List<ExecutionListItem> executions = getExecutionListItems(sm);

                    System.out.println("\t- Total: " + executions.size());
                    executions.forEach(ex -> {
                        System.out.println("\t\t-Start: " + ex.getStartDate());
                        System.out.println("\t\t-Stop: " + ex.getStopDate());
                        System.out.println("\t\t-Name: " + ex.getName());
                        System.out.println("\t\t-Status: " + ex.getStatus());
                        System.out.println();
                    });

                    if ("BasicCalculatorWithAddition".equals(sm.getName())){
                        this.stateMachine =  sm;
                    }
                });
            }

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means" +
                    " your request made it to Amazon Step Functions, but was" +
                    " rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means " +
                    "the client encountered a serious internal problem while " +
                    "trying to communicate with Step Functions, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }

    }

    public Boolean isConnected(){
        return this.stateMachine != null;
    }

    public String execute(String operation, Integer firstNumber, Integer secondNumber){
        StartExecutionRequest startExecutionRequest = new StartExecutionRequest();
        String executionName = UUID.randomUUID().toString();
        startExecutionRequest.setName(executionName);
        startExecutionRequest.setInput("{\n" +
                "  \"operator\": \""+operation+"\",\n" +
                "  \"operands\": ["+firstNumber+", "+secondNumber+"]\n" +
                "}");
        String stateMachineArn = this.stateMachine.getStateMachineArn();
        startExecutionRequest.setStateMachineArn(stateMachineArn);
        this.sfnClient.startExecution(startExecutionRequest);
        return executionName;
    }

    public String getExecutionStatus(String executionName){
        ExecutionListItem theRightExecution = getExecutionByName(executionName);
        System.out.println("\t\t-Start: " + theRightExecution.getStartDate());
        System.out.println("\t\t-Stop: " + theRightExecution.getStopDate());
        System.out.println("\t\t-Name: " + theRightExecution.getName());
        System.out.println("\t\t-Status: " + theRightExecution.getStatus());
        return theRightExecution.getStatus();
    }

    public String getExecutionResult(String executionName){
        ExecutionListItem theRightExecution = getExecutionByName(executionName);
        DescribeExecutionRequest describeExecutionRequest = new DescribeExecutionRequest();
        describeExecutionRequest.setExecutionArn(theRightExecution.getExecutionArn());
        DescribeExecutionResult executionResponse = this.sfnClient.describeExecution(describeExecutionRequest);
        System.out.println("Execution completed");
        return executionResponse.getOutput();
    }

    private ExecutionListItem getExecutionByName(String executionName) {
        List<ExecutionListItem> executionListItems = getExecutionListItems(this.stateMachine);
        ExecutionListItem theRightExecution = executionListItems.stream().
                map(executionListItem -> {
                    return executionListItem.getName().equals(executionName) ? executionListItem : null;
                }).
                findFirst().get();
        if (theRightExecution == null) {
            throw new RuntimeException("Could not find execution with name "+executionName);
        }
        return theRightExecution;
    }

    private List<ExecutionListItem> getExecutionListItems(StateMachineListItem sm) {
        ListExecutionsRequest listRequest = new
                ListExecutionsRequest().withStateMachineArn(sm
                .getStateMachineArn());
        ListExecutionsResult listExecutionsResult = sfnClient
                .listExecutions(listRequest);
        List<ExecutionListItem> executions = listExecutionsResult
                .getExecutions();
        return executions;
    }

    private List<StateMachineListItem> getStateMachineListItems() {
        ListStateMachinesResult listStateMachinesResult = sfnClient.
                listStateMachines(new ListStateMachinesRequest());

        List<StateMachineListItem> stateMachines = listStateMachinesResult
                .getStateMachines();
        return stateMachines;
    }
}
