# Pet Store API Test Automation Framework

A comprehensive test automation framework for the Pet Store API, built with Java, RestAssured, and Cucumber for BDD testing.

## Table of Contents
1. [Project Setup](#project-setup)
2. [Dependencies](#dependencies)
3. [Project Structure](#project-structure)
4. [Implementation Steps](#implementation-steps)
5. [Running Tests](#running-tests)
6. [Troubleshooting](#troubleshooting)

## Project Setup

### 1. Prerequisites
- Java JDK 11 or later
- Maven 3.8.1 or later
- IntelliJ IDEA (or preferred Java IDE)
- Git (for version control)

### 2. Clone the Repository
```bash
git clone git@github.com:ZoranNik91/pet-store-api-taf.git
cd pet-store-api-taf
```

### 3. Build the Project
```bash
mvn clean install
```

## Dependencies

The project uses the following key dependencies:

- **RestAssured** - For API testing
- **Cucumber** - For BDD test implementation
- **JUnit 5** - Test framework
- **Lombok** - For reducing boilerplate code

## Project Structure

```
pet-store/
├── src/
│   ├── main/
│   │   ├── java/zoran/
│   │   │   ├── api/              # API clients
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── models/           # DTOs and entities
│   │   │   └── utils/            # Utility classes
│   │   └── resources/
│   └── test/
│       ├── java/zoran/
│       │   ├── steps/            # Step definitions
│       │   └── runners/          # Test runners
│       └── resources/
│           ├──features/          # Feature files
│           └── test-data/images/ # image files
└── pom.xml
```

## Implementation Details

### API Clients

The framework includes API clients for different endpoints:
- `PetApiClient` - For pet-related operations
- `StoreApiClient` - For store and order operations
- `UserApiClient` - For user management operations

### Step Definitions

Step definitions are organized by functionality:
- `PetAPI` - Pet-related test steps
- `StoreAPI` - Store and order test steps
- `UserAPI` - User management test steps

### Test Data Generation

Utility classes provide test data generation:
- `PetGenerator` - Generates random pet data
- `UserGenerator` - Generates random user data
- `OrderGenerator` - Generates random order data

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Feature
```bash
mvn test -Dcucumber.filter.tags="@pet"
```

### Generate Test Report

#### 1. Run Tests with Allure Listener
```bash
mvn clean test
```

#### 2. Generate Allure Report
```bash
mvn allure:report
```

#### 3. Serve Allure Report Locally
```bash
mvn allure:serve
```

## Test Execution Reports

Test execution reports are generated in the following directories:
- **Allure Results**: `target/allure-results`
- **Allure Report**: `target/site/allure-maven-plugin` (after running `allure:report`)

The Allure report provides a detailed, interactive view of test results, including:
- Test execution history
- Step-by-step test execution
- Screenshots and attachments
- Environment information
- Test categorization and grouping

To view the report, run `mvn allure:serve` and open the provided URL in your browser.

## Troubleshooting

### Common Issues

1. **Dependency Conflicts**
   - Check for duplicate dependencies in `pom.xml`
   - Use `mvn dependency:tree` to identify conflicts

2. **Test Failures**
   - Verify the Petstore API is available
   - Check network connectivity
   - Verify test data matches API expectations

3. **Compilation Errors**
   - Ensure Java version matches in `pom.xml` and IDE
   - Run `mvn clean install` to refresh dependencies

## Best Practices

1. **Test Data Management**
   - Use data generators for test data
   - Clean up test data after tests
   - Use unique identifiers to avoid conflicts

2. **API Client Design**
   - Keep API clients focused on single responsibility
   - Handle common authentication/headers in base class
   - Add retry logic for flaky tests

3. **Test Structure**
   - Organize tests by feature/functionality
   - Use tags for test categorization
   - Keep step definitions clean and reusable

4. **Error Handling**
   - Implement proper error handling in API clients
   - Add meaningful assertions
   - Include error scenarios in test coverage
