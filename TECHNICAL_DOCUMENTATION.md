# Risk Scoring Service - Technical Documentation

## System Architecture
The Risk Scoring Service is implemented as a Spring Boot microservice that follows a layered architecture pattern with clear separation of concerns between controllers, services, and data access layers.

## Technology Stack
- **Java 17** - Primary programming language
- **Spring Boot 3.5.7** - Framework for building the microservice
- **Spring Data MongoDB** - Data persistence
- **Spring Validation** - Request validation
- **Lombok** - Boilerplate code reduction
- **JUnit 5** - Testing framework
- **Swagger/OpenAPI** - API documentation
- **Maven** - Dependency management and build tool

## Project Structure
```
src/main/java/com/risk/scoring/
├── controller/          # REST API endpoints
├── service/             # Business logic implementations
│   └── impl/            # Service implementations
├── model/               # Domain models and DTOs
│   ├── dto/             # Data Transfer Objects
│   └── enums/           # Enumeration types
├── repository/          # Data access interfaces
└── config/              # Configuration classes
```

## Core Components

### 1. REST Controller Layer
- **RiskScoringController**: Handles all REST API endpoints for risk calculation, customer risk profiles, and anomaly detection
- Exposes endpoints for:
  - POST /api/v1/risk/calculate - Calculate risk score for a transaction
  - GET /api/v1/risk/customer/{customerId}/score - Get customer risk profile
  - GET /api/v1/risk/anomalies - Get detected anomalies

### 2. Service Layer
- **RiskScoringService**: Main interface for risk scoring operations
- **RiskScoringServiceImpl**: Implementation of risk scoring logic
- **RiskFactorService**: Interface for individual risk factor calculations
- **RiskFactorService implementations**:
  - TransactionRiskServiceImpl
  - CustomerBehaviorRiskServiceImpl
  - VelocityRiskServiceImpl
  - GeographicRiskServiceImpl
  - MerchantRiskServiceImpl

### 3. Data Models
- **RiskCalculationRequest/Response**: DTOs for API request/response
- **RiskAssessment**: Main entity representing a complete risk assessment
- **RiskFactors**: Individual risk factor scores
- **CustomerProfileData**: Cached customer data for risk calculations
- **VelocityData**: Transaction velocity information
- **Location**: Geographical location data
- **RiskFlag**: Specific flags triggered during risk assessment
- **Enums**: RiskLevel, Decision, Severity, KycStatus, AccountStatus

### 4. Risk Calculation Algorithm

#### Overall Process
1. Receive RiskCalculationRequest with transaction data
2. Calculate individual risk factors using specialized services
3. Aggregate scores using weighted formula
4. Determine risk level and decision based on thresholds
5. Generate risk flags for notable risk factors
6. Calculate approval confidence
7. Return RiskCalculationResponse

#### Risk Factor Calculations

##### Transaction Risk Factor (30% weight)
- Score based on amount relative to customer averages/limits
- Additional points for unusual timing (outside 6 AM - 10 PM)
- Bonus points for high-risk channels

##### Customer Behavior Risk Factor (25% weight)
- New customer accounts (< 30 days)
- Customer fraud history
- Multiple failed transactions (> 3 in 7 days)
- Dormant account status

##### Velocity Risk Factor (20% weight)
- Transactions in last hour (> 20)
- Transactions in last day (> 100 or > 50)

##### Geographic Risk Factor (15% weight)
- Impossible travel detection
- Countries not in allowed list
- High-risk country transactions

##### Merchant Risk Factor (10% weight)
- High-risk merchant categories
- Medium-risk merchant categories

## Configuration
The service can be configured through the `application.yaml` file with properties for:
- Server port
- MongoDB connection details
- Logging levels
- Validation rules

## API Endpoints

### Calculate Risk Score
- **URL**: POST /api/v1/risk/calculate
- **Request Body**: RiskCalculationRequest
- **Response**: RiskCalculationResponse
- **Description**: Calculates risk score for a transaction

### Get Customer Risk Profile
- **URL**: GET /api/v1/risk/customer/{customerId}/score
- **Response**: CustomerRiskProfileResponse
- **Description**: Retrieves customer's risk profile including current score and history

### Get Anomalies
- **URL**: GET /api/v1/risk/anomalies
- **Parameters**: timeWindow (optional)
- **Response**: AnomaliesResponse
- **Description**: Retrieves detected anomalies

## Data Persistence
Currently, the service uses in-memory data structures. For production deployment, it should integrate with:
- MongoDB for customer profiles, risk rules, and risk event storage
- Redis for caching frequently accessed data
- Kafka for event-driven processing and inter-service communication

## Event-Driven Architecture
The service is designed to work within an event-driven architecture:
- Consumes transaction-validated events from Kafka
- Produces risk-score-calculated events to Kafka
- Integrates with other services through domain events

## Security
- Input validation using Jakarta Bean Validation
- Proper error handling and logging
- Secure coding practices

## Testing
- Unit tests for risk calculation logic
- Integration tests for API endpoints
- Service layer tests for business logic

## Deployment
- Standalone JAR file deployment
- Docker container support
- Kubernetes deployment ready
- Health checks and monitoring endpoints

## Performance Considerations
- Caching of customer profiles and risk rules
- Efficient risk calculation algorithms
- Asynchronous processing where appropriate
- Connection pooling for database access