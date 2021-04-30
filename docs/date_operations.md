# Date operations

- [Create a dynamic date](#create-a-date-n-days-in-the-past)
    - [Create a date `n` days in the past](#create-a-date-n-days-in-the-past)
    - [Create a date `n` months in the past](#create-a-date-n-months-in-the-past)
    - [Create a date `n` years in the past](#create-a-date-n-years-in-the-past)
    - [Create a date `n` days in the future](#create-a-date-n-days-in-the-future)
    - [Create a date `n` months in the future](#create-a-date-n-months-in-the-future)
    - [Create a date `n` years in the future](#create-a-date-n-years-in-the-future)
- [Compare a date(time) of the JSON with a date of the context](#compare-a-datetime-of-the-json-with-a-date-of-the-context)
- [Examples](#examples)

# Create a dynamic date

## Create a date `n` days in the past

Creates a date from now minus given number of days and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 days in the past
  Given that a date {int} days in the past is stored as {string}
```

## Create a date `n` months in the past

Creates a date from now minus given number of months and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 months in the past
  Given that a date {int} months in the past is stored as {string}
```

## Create a date `n` years in the past

Creates a date from now minus given number of years and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 years in the past
  Given that a date {int} years in the past is stored as {string}
```

## Create a date `n` days in the future

Creates a date from now plus given number of days and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 days in the future
  Given that a date {int} days in the future is stored as {string}
```

## Create a date `n` months in the future

Creates a date from now plus given number of months and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 months in the future
  Given that a date {int} months in the future is stored as {string}
```

## Create a date `n` years in the future

Creates a date from now plus given number of years and stores it in `ScenarioContext` under given name.

```gherkin
Scenario: create a date 3 years in the future
  Given that a date {int} years in the future is stored as {string}
```

# Compare a date(time) of the JSON with a date of the context

With the special JSON-Unit matcher `${json-unit.matches:isDateOfContext}<var>` is it possible to compare a date or a datetime with a date in the context.

**_Please note that the time is NOT compared!_**

# Examples

Examples can be found at [src/test/resources/features/date_operations/date_operations.feature](../src/test/resources/features/date_operations/date_operations.feature)