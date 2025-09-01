# BibleVerse API

A RESTful API service that generates random Bible verses using Spring Boot and AWS Lambda. The service provides both KJV (King James Version) and SBLGNT (Greek New Testament) text for each verse.

## Overview

BibleVerse is a cloud-native application that provides simple API endpoints to fetch random Bible verses. The service is deployed as AWS Lambda functions using Spring Cloud Function and uses S3 for data storage.

## Features

- **Random Bible Verse Generation**: Returns random verses from the entire Bible
- **Multiple Text Versions**: Provides both KJV and SBLGNT (Greek) text
- **RESTful API**: Clean HTTP endpoints with proper JSON responses
- **Serverless Architecture**: AWS Lambda with API Gateway v2
- **Cloud-Native**: S3 integration for data storage
- **Health Monitoring**: Built-in health check endpoint
- **CORS Support**: Cross-origin resource sharing enabled

## Technology Stack

- **Backend Framework**: Spring Boot 3.4.5
- **Language**: Java 21
- **Cloud Platform**: AWS
  - Lambda (serverless compute)
  - API Gateway v2 (HTTP API)
  - S3 (data storage)
  - Route 53 (DNS)
  - ACM (SSL certificates)
- **Infrastructure**: AWS CDK (TypeScript)
- **Dependencies**:
  - Spring Cloud Function
  - AWS SDK for Java v2
  - Lombok
  - Joda Time
  - JSON.org

## API Endpoints

### Base URL
```
https://bibleverse.thonbecker.com
```

### Endpoints

#### 1. Random Bible Verse
**GET** `/`

Returns a random Bible verse with both KJV and SBLGNT text.

**Response:**
```json
{
  "book": "John",
  "chapter": "3",
  "verse": "16",
  "text": {
    "KJV": "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
    "SBLGNT": "Οὕτως γὰρ ἠγάπησεν ὁ θεὸς τὸν κόσμον, ὥστε τὸν υἱὸν τὸν μονογενῆ ἔδωκεν, ἵνα πᾶς ὁ πιστεύων εἰς αὐτὸν μὴ ἀπόληται ἀλλ᾽ ἔχῃ ζωὴν αἰώνιον."
  }
}
```

#### 2. Health Check
**GET** `/about`

Returns service health information and build details.

**Response:**
```json
{
  "status": "healthy",
  "packageName": "bibleverse",
  "version": "2.0.0",
  "time": "2024-01-15T10:30:00Z"
}
```

### Response Headers
All endpoints return:
- `Content-Type: application/json`
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, OPTIONS`
- `Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Access-Control-Allow-Origin, Access-Control-Allow-Methods`

## Setup and Deployment

### Prerequisites

- **Java 21** or higher
- **Maven 3.8** or higher
- **Node.js 18** or higher (for CDK)
- **AWS CLI** configured with appropriate permissions
- **AWS CDK** installed globally: `npm install -g aws-cdk`

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd BibleVerse
   ```

2. **Configure AWS credentials**
   ```bash
   aws configure
   ```

3. **Build the application**
   ```bash
   mvn clean package
   ```

4. **Synthesize CDK template**
   ```bash
   cd .infrastructure
   npm install
   cdk synth
   ```

### Deployment

1. **Deploy to AWS**
   ```bash
   cd .infrastructure
   cdk deploy
   ```

2. **Verify deployment**
   ```bash
   curl https://bibleverse.thonbecker.com/about
   ```

### Infrastructure Components

The CDK stack creates:
- **Lambda Functions**: Two functions for different endpoints
- **API Gateway v2**: HTTP API with custom domain
- **S3 Bucket**: Data storage for Bible text files
- **Route 53**: DNS configuration
- **ACM Certificate**: SSL certificate for HTTPS

## Development

### Code Quality

The project uses several tools to maintain code quality:
- **Spotless**: Code formatting with Palantir Java style
- **SortPOM**: Maven POM file organization
- **Lombok**: Reduces boilerplate code

### Project Structure

```
BibleVerse/
├── src/main/java/com/thonbecker/bibleverse/
│   ├── functions/           # Spring Cloud Functions
│   │   ├── AboutHandler.java
│   │   └── RandomBibleVerseHandler.java
│   ├── model/              # Data models
│   │   ├── AboutResponse.java
│   │   ├── BookData.java
│   │   └── RandomBibleVerseResponse.java
│   └── service/            # Business logic
│       └── FileService.java
├── .infrastructure/        # AWS CDK code
│   └── lib/
│       └── bibleverse-stack.ts
└── target/                # Build artifacts
```

### Testing

Run the test suite:
```bash
mvn test
```

### Code Formatting

Format code using Spotless:
```bash
mvn spotless:apply
```

## Recent Improvements

### API Gateway Content Type Fix

**Issue**: API Gateway was not returning `Content-Type: application/json` headers.

**Solution**: Added response parameters to HTTP Lambda integrations in the CDK stack:

```typescript
new HttpLambdaIntegration('Integration', function, {
  responseParameters: {
    '200': {
      'Content-Type': "'application/json'"
    },
    '500': {
      'Content-Type': "'application/json'"
    }
  }
})
```

### Handler Improvements

- **AboutHandler**: Now returns `AboutResponse` object instead of manually serialized string
- **Consistent JSON handling**: Both handlers now use Spring's automatic JSON serialization
- **Better error handling**: Added proper error response configurations

## Monitoring and Logs

- **CloudWatch Logs**: Lambda function logs with 3-day retention
- **API Gateway Access Logs**: HTTP request/response logging
- **Health Monitoring**: `/about` endpoint for service health checks

## Security

- **HTTPS Only**: All endpoints use SSL/TLS encryption
- **CORS Configuration**: Properly configured for web applications
- **S3 Security**: Private bucket with IAM-based access control
- **Lambda Security**: Minimal IAM permissions following least privilege principle

## License

This project is licensed under the MIT License.
