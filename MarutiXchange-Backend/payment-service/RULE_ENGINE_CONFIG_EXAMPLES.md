# Rule Engine Integration Configuration Examples

## Production Configuration

```properties
# Rule Engine Integration - Production
app.rule-engine.url=http://rule-engine-service:8055/rules/evaluate
app.rule-engine.timeout-ms=10000
app.rule-engine.retry-attempts=3
app.rule-engine.enabled=true
```

## Development Configuration

```properties
# Rule Engine Integration - Development (Local)
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.timeout-ms=5000
app.rule-engine.retry-attempts=3
app.rule-engine.enabled=true
```

## Testing Configuration

```properties
# Rule Engine Integration - Testing (Disabled for unit tests)
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.timeout-ms=5000
app.rule-engine.retry-attempts=1
app.rule-engine.enabled=false
```

## Docker Compose Example

If using Docker Compose, configure the Rule Engine service:

```yaml
version: '3.8'

services:
  payment-service:
    image: payment-service:latest
    container_name: payment-service
    ports:
      - "8069:8069"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/marutixchange_payment
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: admin
      APP_RULE_ENGINE_URL: http://rule-engine:8055/rules/evaluate
      APP_RULE_ENGINE_ENABLED: "true"
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
    depends_on:
      - mysql
      - rule-engine
    networks:
      - marutixchange-network

  rule-engine:
    image: rule-engine:latest
    container_name: rule-engine
    ports:
      - "8055:8055"
    environment:
      SERVER_PORT: 8055
    networks:
      - marutixchange-network

  mysql:
    image: mysql:8.0
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: admin
      MYSQL_DATABASE: marutixchange_payment
    networks:
      - marutixchange-network

  eureka:
    image: eureka:latest
    container_name: eureka
    ports:
      - "8761:8761"
    networks:
      - marutixchange-network

networks:
  marutixchange-network:
    driver: bridge
```

## Environment Variable Configuration

For containerized deployments, use environment variables:

```bash
# Set environment variables
export APP_RULE_ENGINE_URL=http://rule-engine-service:8055/rules/evaluate
export APP_RULE_ENGINE_TIMEOUT_MS=10000
export APP_RULE_ENGINE_RETRY_ATTEMPTS=3
export APP_RULE_ENGINE_ENABLED=true

# Run the application
java -jar payment-service-1.0.0.jar
```

## Kubernetes ConfigMap Example

For Kubernetes deployments:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-service-config
  namespace: marutixchange
data:
  application.properties: |
    spring.application.name=payment-service
    server.port=8069
    
    spring.datasource.url=jdbc:mysql://mysql-service:3306/marutixchange_payment
    spring.datasource.username=root
    spring.datasource.password=${DB_PASSWORD}
    
    app.rule-engine.url=http://rule-engine-service:8055/rules/evaluate
    app.rule-engine.timeout-ms=10000
    app.rule-engine.retry-attempts=3
    app.rule-engine.enabled=true
    
    eureka.client.service-url.defaultZone=http://eureka-service:8761/eureka/
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: payment-service
  namespace: marutixchange
spec:
  replicas: 3
  selector:
    matchLabels:
      app: payment-service
  template:
    metadata:
      labels:
        app: payment-service
    spec:
      containers:
      - name: payment-service
        image: payment-service:1.0.0
        ports:
        - containerPort: 8069
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        volumeMounts:
        - name: config
          mountPath: /config
      volumes:
      - name: config
        configMap:
          name: payment-service-config
```

## Profile-Specific Configurations

### application-dev.properties
```properties
# Development
app.rule-engine.url=http://localhost:8055/rules/evaluate
app.rule-engine.enabled=true
app.rule-engine.retry-attempts=3
logging.level.com.marutixchange=DEBUG
```

### application-staging.properties
```properties
# Staging
app.rule-engine.url=http://staging-rule-engine:8055/rules/evaluate
app.rule-engine.enabled=true
app.rule-engine.retry-attempts=3
logging.level.com.marutixchange=INFO
```

### application-prod.properties
```properties
# Production
app.rule-engine.url=http://prod-rule-engine:8055/rules/evaluate
app.rule-engine.enabled=true
app.rule-engine.retry-attempts=5
app.rule-engine.timeout-ms=15000
logging.level.com.marutixchange=WARN
```

## Spring Boot Configuration Priority

Configuration is loaded in the following order (highest to lowest priority):

1. Command-line arguments
2. System environment variables
3. System properties
4. `application-{profile}.properties`
5. `application.properties`

Example with all methods:

```bash
# Method 1: Command-line argument
java -jar payment-service.jar \
  --app.rule-engine.url=http://custom-rule-engine:8055/rules/evaluate

# Method 2: Environment variable
export APP_RULE_ENGINE_URL=http://env-rule-engine:8055/rules/evaluate
java -jar payment-service.jar

# Method 3: Properties file
# In application.properties:
app.rule-engine.url=http://default-rule-engine:8055/rules/evaluate
java -jar payment-service.jar
```

## SSL/TLS Configuration

For secure communication with Rule Engine:

```properties
# Rule Engine Integration with SSL
app.rule-engine.url=https://rule-engine-service:8055/rules/evaluate

# Client SSL Configuration
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
```

## Advanced Configuration

### Custom RestTemplate for Rule Engine

If you need custom configuration, create a bean:

```java
@Configuration
public class RuleEngineConfig {

    @Bean("ruleEngineRestTemplate")
    public RestTemplate ruleEngineRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

### Request Timeout Configuration

```properties
# HTTP client configuration
spring.http.client.connect-timeout=5000
spring.http.client.read-timeout=10000

# Rule Engine specific timeout
app.rule-engine.timeout-ms=10000
```

## Monitoring Configuration

### Actuator Endpoints

```properties
# Enable health checks
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always

# Custom metrics
management.metrics.tags.application=payment-service
management.metrics.tags.environment=production
```

### Logging Configuration

```properties
# Log Rule Engine interactions
logging.level.com.marutixchange.payment_service.client=DEBUG
logging.level.org.springframework.web.client=DEBUG

# Log HTTP details
logging.level.org.springframework.http=DEBUG
logging.level.org.springframework.web=INFO
```
