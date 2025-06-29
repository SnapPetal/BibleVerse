# BibleVerse API

A RESTful API service that generates random Bible verses using Spring Boot and AWS Lambda.

## Overview

BibleVerse is a cloud-native application that provides a simple API endpoint to fetch random Bible verses. The service is deployed as an AWS Lambda function and uses Spring Boot for its backend implementation.

## Features

- Random Bible verse generation
- RESTful API endpoint
- AWS Lambda deployment
- Spring Boot backend
- S3 integration for data storage

## Technology Stack

- **Backend**: Spring Boot 3.4.1
- **Language**: Java 21
- **Cloud**: AWS Lambda
- **Dependencies**:
  - Spring Cloud Function
  - AWS SDK for Java
  - Lombok
  - Joda Time
  - JSON.org

## Setup and Deployment

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- AWS CLI configured with appropriate permissions
- AWS CDK installed

### Local Development

1. Clone the repository
2. Configure your AWS credentials
3. Run the following commands:
   ```bash
   mvn clean package
   cdk synth
   ```

### Deployment

To deploy to AWS:

```bash
# Synthesize the CloudFormation template
cdk synth

# Deploy to AWS
cdk deploy
```

## API Documentation

The API is available at: [https://bibleverse.thonbecker.com](https://bibleverse.thonbecker.com)

## Development

The project uses Spotless for code formatting and follows the Palantir Java formatting style.

## License

This project is licensed under the MIT License.
