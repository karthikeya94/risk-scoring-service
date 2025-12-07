# Risk Scoring Service - Business Functionality

## Overview
The Risk Scoring Service is a critical component of the financial transaction processing system that calculates real-time risk scores for transactions using a rule-based approach. It helps identify potentially fraudulent activities and makes informed decisions about transaction approval.

## Core Business Functions

### 1. Risk Calculation
The primary function of the service is to calculate risk scores for financial transactions based on multiple weighted factors:

#### Risk Factors (Weighted)
1. **Transaction Risk Factor (30%)**
   - Based on transaction amount relative to customer's average transaction amount
   - Considers daily limits and unusual transaction amounts
   - Additional scoring for transactions occurring at unusual times
   - Bonus points for high-risk channels

2. **Customer Behavior Risk Factor (25%)**
   - New customer accounts (less than 30 days old)
   - Customer fraud history
   - Multiple failed transactions in the last 7 days
   - Dormant account status (no transactions in over 90 days)

3. **Velocity Risk Factor (20%)**
   - Number of transactions in the last hour
   - Number of transactions in the last 24 hours
   - Threshold-based scoring for excessive transaction frequency

4. **Geographic Risk Factor (15%)**
   - Impossible travel detection (transactions from distant locations in short time periods)
   - Transactions from countries not in customer's allowed list
   - Transactions from high-risk countries
   - Geographic distance calculations

5. **Merchant Risk Factor (10%)**
   - Merchant category risk classification (Drugs, Weapons, Gambling, Dark Web)
   - Medium-risk merchant categories (Cash Advance, Wire Transfer, Crypto)
   - Merchant chargeback rates
   - Newly registered merchants

#### Risk Score Aggregation
- Scores from all factors are combined using weighted formula
- Final score ranges from 0-100
- Risk levels determined based on score thresholds:
  - 0-20: LOW → APPROVE
  - 21-50: MEDIUM → APPROVE (with monitoring)
  - 51-75: HIGH → MANUAL_REVIEW
  - 76-100: CRITICAL → REJECT/BLOCK

### 2. Customer Risk Profiling
- Maintains historical risk profiles for customers
- Tracks risk score trends over time
- Stores monthly statistics for customer behavior analysis
- Updates customer risk profiles based on significant changes

### 3. Risk Rule Management
- Configurable risk rules and thresholds
- Administrative interface for updating risk scoring parameters
- Version control for risk rules

### 4. Anomaly Detection
- Identifies unusual patterns in transaction behavior
- Flags potential fraud indicators
- Tracks geographic anomalies (impossible travel)
- Monitors velocity spikes

## Business Value
- Real-time fraud prevention
- Automated transaction approval decisions
- Regulatory compliance support
- Customer risk monitoring
- Operational efficiency through automated risk assessment

## Target Users
- Financial institutions processing transactions
- Risk management teams
- Compliance officers
- System administrators managing risk rules

## Integration Points
- Transaction Ingestion Service (receives validated transactions)
- Fraud Detection Service (shares risk assessments)
- Compliance Service (provides risk data for compliance checks)
- Notification Service (triggers alerts based on risk levels)
- Analytics Service (provides risk metrics for reporting)