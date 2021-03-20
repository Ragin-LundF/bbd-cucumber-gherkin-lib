# JSONUnit Matcher support

- [Extension of JSON Unit Matcher](#extension-of-json-unit-matcher)
    - [Simple matcher](#simple-matcher)
    - [Matcher with parameter](#matcher-with-parameter)

## Extension of JSON Unit Matcher

It is possible to extend the JSON matchers by creating a new matcher and extending the `org.hamcrest.BaseMatcher` class and implementing the `com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher` interface.

After they are created, you have to add them to the `@ContextConfiguration` classes definition.
See [CreateContextHooks.java](../src/test/java/com/ragin/bdd/cucumbertests/hooks/CreateContextHooks.java) for an example how the configuration should look like.

There are also some examples of custom matchers at [src/test/java/com/ragin/bdd/cucumbertests/hooks](../src/test/java/com/ragin/bdd/cucumbertests/hooks)

### Simple matcher
A simple matcher to validate the current object as it is, can look like this:

```java
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

@Component
public class DividableByTwoMatcher extends BaseMatcher<Object> implements BddCucumberJsonMatcher {
    public boolean matches(Object item) {
        if (StringUtils.isNumeric(String.valueOf(item))) {
            // never do that, but it should show something ;)
            return Integer.parseInt((String) item) % 2 == 0;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        // nothing to describe here
    }

    @Override
    public String matcherName() {
        return "isDividableByTwo";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }
}
```

Now you can use this matcher with the following statement in your expected JSON:

```json
{
  "number": "${json-unit.matches:isDividableByTwo}"
}
```

The `matcherName()` result is now part of the `json-unit.matches:` definition.

### Matcher with parameter

If you need parameter, you can implement also the `net.javacrumbs.jsonunit.core.ParametrizedMatcher` interface.
If there are several arguments, you can pass the arguments as JSON to the matcher and parse it here.

```java
import com.ragin.bdd.cucumber.matcher.BddCucumberJsonMatcher;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.stereotype.Component;

@Component
public class DividableByNumberMatcher extends BaseMatcher<Object> implements ParametrizedMatcher, BddCucumberJsonMatcher {
    private String parameter;

    public boolean matches(Object item) {
        if (StringUtils.isNumeric(String.valueOf(item))) {
            // never do that, but it should show something ;)
            return Integer.parseInt((String) item) % Integer.parseInt(parameter) == 0;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        // nothing to describe here
    }

    @Override
    public String matcherName() {
        return "isDividableByNumber";
    }

    @Override
    public Class<? extends BaseMatcher<?>> matcherClass() {
        return this.getClass();
    }

    @Override
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }
}
```

To pass the parameter to the matcher, the JSON has to look like this:


```json
{
  "number": "${json-unit.matches:isDividableByNumber}5"
}
```

If you want to pass a JSON, you have to do it with single quotes:


```json
{
  "number": "${json-unit.matches:isDividableByNumber}{\"myarg1\": \"A\"}"
}
```
