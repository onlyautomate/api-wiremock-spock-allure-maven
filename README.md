This project is a sample **Test Automation Framework** built using **Spock**, **RestAssured**, **Maven** and **Allure** to write automated API tests in **Groovy**.

Additionally, it also demonstrates the use of **WireMock** for stubbing API endpoints, allowing you to simulate various server responses during test execution.

## Key features

- Spock specifications, integrated with RestAssured (for calling API endpoints) and WireMock (for stubbing API endpoints)
- Programmatic stubbing as well as stubbing using json mappings
- Utilization of scenario state for efficient WireMock stubbing
- Custom Spock annotation-driven extensions and global extension, integrated through the service loader, for enhanced test execution control
- Data-driven tests with 'where' block including the ability to automatically skip specific iterations from data table
- Progress tracker of all applicable specifications being executed during a test execution session including status, iterations and reruns, if any
- Quick and easy addition of individual APIs and their endpoints using a base class to handle shared functionality and configurations with a custom RestAssured filter
- Reusable methods enable efficient API calls, ensuring scalability and ease of maintenance
- Fully integrated Allure reporting framework, with default allure lifecycle, to automatically generate a detailed, consolidated test report including detailed logs at the end of the execution

## Prerequisites

- **Java** 21 or higher
- **Maven** 3.9.6 or higher

## Getting Started

1. Clone or download the repository.
2. Open the project in IntelliJ IDEA (the bundled Maven version will be used by default)
3. Build the project using maven.
4. In **IntelliJ IDEA**, go to **Run** → **Edit Configurations**.
5. Choose any of the predefined **Maven run/debug configurations** and click **Run** ▶️ or **Debug** 🐞.

## Viewing the Allure Report

At the end of the test execution, follow the console output, which will direct you to the location of detailed time-stamped Allure report in the `testReports/<timestamp>/allure-report` directory. The output should look something like this:

![Screenshot](/allure-testreport-screenshot.png)