package hellocucumber;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StateMachineListItem;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StepDefinitions {
    private String operation;
    private Integer finalNumber;
    private Integer secondNumber;
    private String executionName;
    private CalculatorFacade calculatorFacade;

    @Given("the calculator system is ready")
    public void the_calculator_system_is_ready() {
        this.calculatorFacade = new CalculatorFacade();
        assertTrue(this.calculatorFacade.isConnected());
    }

    @Given("the operation is {string}")
    public void the_operation_is(String operation) {
        this.operation = operation;
    }

    @Given("the first number is {int}")
    public void the_first_number_is(Integer finalNumber) {
        this.finalNumber = finalNumber;
    }

    @Given("the second number is {int}")
    public void the_second_number_is(Integer secondNumber) {
        this.secondNumber = secondNumber;
    }

    @When("the command is executed")
    public void the_command_is_executed() {
        this.executionName = this.calculatorFacade.execute(this.operation, this.finalNumber, this.secondNumber);
    }

    @Then("it should respond with the number {double}")
    public void it_should_respond_with_the_number(Double number) throws InterruptedException {
        String status = "";
        int maxTryCount = 59;
        int count = 0;
        Boolean shouldContinue = true;
        while(shouldContinue) {
            Thread.sleep(1000);
            status = this.calculatorFacade.getExecutionStatus(this.executionName);
            if("SUCCEEDED".equals(status)){
                String executionResult = this.calculatorFacade.getExecutionResult(this.executionName);
                assertEquals("{\"result\":"+number+"}", executionResult);
                shouldContinue = false;
            } else if (count++ >= maxTryCount){
                throw new RuntimeException("Step function execution didn't finish. It took more than one minute running");
            }
        }
    }

}
