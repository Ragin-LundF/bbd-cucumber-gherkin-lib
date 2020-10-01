# Release 1.20.3

Now it is allowed to use full paths instead of only URIs.
This may help if between some external systems has to be called.

The library checks if the path starts with `http://` or `https://`.
In these cases, it does not add `localhost`.

# Release 1.20.2

This release fixes the problem, that the sentence to set a header value does not resolve the value first from the context.

Now it is possible to use static values or (if the value matches to the context) previously stored parameters.

Example:

[src/test/resources/features/header/](src/test/resources/features/header/)

# Release 1.20.1

The REST template has thrown an exception in case of `5xx` response codes.

This version fixes the issue and returns the correct HTTP code and message. 

# Release 1.20.0

## Introducing user

### Define user(s)
With the following sentence it is possible to define multiple users:
```gherkin
Feature: User features
  Background:
    Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    | johana_doe  | my_auth_token_for_johana_doen |
```

Now every scenario in this feature can use the user with:
```gherkin
  Scenario: Using authorized user john_doe
    Given that the user is "john_doe"
```

The library selects the right token from the given list and executes the calls with this user.
It is also possible to define both in the `Background` specification. Then all tests will have this user as default:

```gherkin
Feature: User features in global context
  Background:
    Given that the following users and tokens are existing
    | john_doe    | my_auth_token_for_john_doe    |
    And that the user is "john_doe"
```

Please have a look to the examples at: [src/test/resources/features/user/](src/test/resources/features/user/)

# Release 1.19.0

## Fewer sentences but still compatible and more possibilities
This release introduces the parameter `{httpMethod}` which replaces all sentences with an HTTP method in the name.
Allowed values are:
- GET
- POST
- DELETE
- PUT
- PATCH

This reduces the number of Gherkin sentences by 24 and adds 24 new possible sentences because the shorter GET sentences are now also available for all other methods.


## Validate only special fields of the response body
### Validation of one field
```gherkin
  Scenario: Validate field of the body
    Then I ensure that the body of the response contains a field "list[0]" with the value "First"
    And I ensure that the body of the response contains a field "$.list[1]" with the value "Second"
    Then I ensure that the body of the response contains a field "shouldNotExist" with the value "@bdd_lib_not_exist"
```

### Validation of multiple fields
```gherkin
  Scenario: Validate multiple fields
    And I ensure that the body of the response contains the following fields and values
    | string           | is a string                 |
    | number           | 12                          |
    | uuid             | ${json-unit.matches:isUUID} |
    | $.number         | @bdd_lib_not 15             |
    | list             | ["First","Second"]          |
    | list[0]          | First                       |
    | $.list[0]        | BDD_TEST_LIST_FIRST_ELEMENT |
    | $.list[1]        | Second                      |
    | object.firstname | John                        |
    | object.lastname  | Doe                         |
    | shouldNotExist   | @bdd_lib_not_exist          |
```

In this case, the fields that should be compared can be given as a data table map.
The first column is the field name, the second the expected value.

### Description
This sentence compares only the given field of the response.
The field can be a JSON path. The library checks if it starts with `$.`.
If it does not start with `$.` it will be added internally.

To test if a field is NOT present, the reserved word `@bdd_lib_not_exist` can be used as the value.

To test if a value is NOT the expected value, the reserved word `@bdd_lib_not ` can be used to negate the comparison.
It is not possible to use a `!` as a negation prefix, because it can also be a valid result.

The library also tries to resolve the value from the context map.
If nothing was found, the original value is used.

It is also possible to use JSON-Matcher (user-defined and bdd-cucumber-lib).
These are written with the notation `${json-unit.matches:isUUID}` (as an example for the UUID-Matcher).

**_ATTENTION: Only unparameterized custom matchers or bdd lib-matchers can be used for field validation!_**

Find examples for this feature under: [src/test/resources/features/body_validation/](src/test/resources/features/body_validation/).


## Reset the scenario context
```gherkin
Scenario: Reset the scenario context
    Given that the stored data in the scenario context map has been reset
```

Reset the context state map.


# Release 1.18.0

## Configure the JSON compare to ignore extra elements in arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays with extra elements
```

_It is also possible to use the `@bdd_lib_json_ignore_new_array_elements` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore new array elements.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


## Configure the JSON compare to ignore the order of arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays in a different order
```

_It is also possible to use the `@bdd_lib_json_ignore_array_order` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore the order of arrays.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


## Configure the JSON compare to ignore extra fields
```gherkin
Scenario:
  Given that the response JSON can contain extra fields
```

_It is also possible to use the `@bdd_lib_json_ignore_extra_fields` annotation on `Feature` or `Scenario` level._


With this sentence or annotation, the JSON comparison will ignore new/not defined fields in the response.

See [src/test/resources/features/flexible_json/](src/test/resources/features/flexible_json/) for examples.


# Release 1.17.0

## Absolute file path support

Files can be added as a relative path to a previously given base path or with an "absolute" path with the prefix `absolutePath:`.
In the last case, the system is using the base classpath as root.

## Validate response HTTP code and body together
```gherkin
Scenario:
  Then I ensure that the response code is 201 and the body is equal to
    """
    {
      "field": "value",
    }
    """
```

In this case, the response status code is part of the sentence, and the JSON is written directly under the sentence and enclosed in three double quotation marks.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.
 

## Validate response HTTP code and body together with a JSON file
```gherkin
Scenario:
  Then I ensure that the response code is 200 and the body is equal to the file "response.json"
```

In this case, the response status code, and the JSON file are written together in one sentence.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.
 

# Release 1.16.0
- Changing the body manipulation with creating numbers from `<number> characters` to `<number> bdd_lib_numbers` (e.g. `10 bdd_lib_numbers`).
- Adding `bdd_lib_uuid` to generate random UUIDs
- Adding a `${json-unit.matches:isValidUUID}` which checks, if the string is a valid UUID
- Fixed Authorization header, that this header is only available once, when it is overwritten
- Adding a lot of tests as examples

# Release 1.15.0
Adding support in the paths to support templates.
If the path contains something like:
```
/api/v1/${dynamicElement}/
```
In this case, the `dynamicElement` will be replaced, if it exists in the ScenarioContext.

Support for adding static key/value pairs to the context:
- [Set a static value to the context](README.md#set-a-static-value-to-the-context)
- [Set multiple static values to the context](README.md#set-multiple-static-values-to-the-context)

# Release 1.14.0

Adding support for the JSON path to the "I set the value of" sentence.
It now also tries to resolve the value from the ScenarioContext.
If nothing is found, it uses the original value. 

# Release 1.13.0

Adding support for JSON path to the "I store" sentence.

The JSON path can be used with:
```
$.firstElement[3].nextElement
```

The library detects, if the path has the prefix `$.`. If it is not available, it adds this prefix.


# Release 1.12.0

- Adding support for adding and manipulating headers.
- Restructured the documentation: HTTP Methods have now their own md files.

# Release 1.11.0

- Adding support for JSON matcher which can compare to values in the ScenarioContext.
  More information: [README.md#json-unit](README.md#json-unit)
- Adding support for own custom matcher.
  More information: [README.md#extension-of-json-unit-matcher](README.md#extension-of-json-unit-matcher)

# Release 1.9.0

- Correction of the sentence `^that the body of the response is$` to `^that the body of the request is$`
