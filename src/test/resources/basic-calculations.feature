Feature: Calculations

  As a student
  I want to have a basic calculator
  So that I can finish my homework quickly

  Scenario: Addition of two numbers
    Given the calculator system is ready
    And the operation is "add"
    And the first number is 4
    And the second number is 3
    When the command is executed
    Then it should respond with the number 7.0