# Configuration

- [Support JUnit 5](#support-junit-5)
- [Base Configuration](#base-configuration)
    - [Support for database-less applications](#support-for-database-less-applications)
    - [Base token definition](#base-token-definition)
    - [Base URL definition](#base-url-definition)
    - [Proxy support](#proxy-support)
    - [Disable SSL verification](#disable-ssl-verification)


## Support JUnit 5
To support JUnit 5 add the `org.junit.vintage:junit-vintage-engine` dependency to your project.
This allows JUnit 5 to execute JUnit 3 and 4 tests.

```groovy
testRuntimeOnly('org.junit.vintage:junit-vintage-engine') {
    because("allows JUnit 3 and JUnit 4 tests to run")
}
```

## Base Configuration

### Support for database-less applications
To configure the library to run database-less, it is necessary to set up the following configuration in the `application.yaml` file:

application.properties:
```properties
cucumberTest.databaseless=true
```

or

application.yaml:
```yaml
cucumberTest:
  databaseless: true
```

If the `databaseless` key is not true or missing, the library tries to instantiate the database related beans.

In some cases it is required to disable the database autoconfiguration of Spring Boot:


application.properties:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

or

application.yaml:
```yaml
spring:
  autoconfigure:
    exclude: org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration, org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

or as annotation:

```java
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
public class Application {
  public static void main (String[] args) {
    ApplicationContext ctx = SpringApplication.run(Application.class, args);
  }
}
```

Please also make sure that the `@ContextConfiguration` annotation does not contain the `DatabaseExecutorService.class`.

### Base token definition
To define a default token those two parameters can be set in the properties:

`application.properties`:
```properties
cucumberTest.authorization.bearerToken.default=default_token_for_authorized_endpoints 
cucumberTest.authorization.bearerToken.noscope=second_token_for_alternatives
```

or

`application.yaml`:
```yaml
cucumberTest:
  authorization:
    bearerToken:
      default: "default_token_for_authorized_endpoints" 
      noscope: "second_token_for_alternatives"
```


### Base URL definition
If the test library should be used for various tests in an outsourced test repository.

It is possible to overwrite the `protocol`, `host` and `port` in the `application.yaml`/`application.properties`:

application.properties:
```properties
cucumberTest.server.protocol=http
cucumberTest.server.host=localhost
cucumberTest.server.port=80
```

or

application.yaml:
```yaml
cucumberTest:
    server:
      protocol: "http"
      host: "localhost"
      port: 80
```

_All parameters are optional. If nothing is being defined, it uses the default `http://localhost:<LocalServerPort>`._

### Proxy support
This can be useful to use the cucumber tests together with [burp-scanner](https://portswigger.net/burp/vulnerability-scanner).

The proxy can be configured with:

application.properties:
```properties
cucumberTest.proxy.host=localhost
cucumberTest.proxy.port=8866
```

or

application.yaml:
```yaml
cucumberTest:
  proxy:
    host: localhost
    port: 8866
```
The host can be an IP or a domain. The port must be higher than 0.
If a condition is not met, the proxy is not set.

_Default is deactivated._

### Disable SSL verification
Along with proxy support, it may be necessary to disable SSL validation as well.

This can be configured via:

application.properties:
```properties
cucumberTest.ssl.disableCheck=true
```

or

application.yaml:
```yaml
cucumberTest:
  ssl:
    disableCheck: true
```

_Default is false._
