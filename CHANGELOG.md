# Release 1.17.0

## Absolute file path support

Files can be added as relative path to a previously given base path or with an "absolute" path with the prefix `absolutePath:`.
In the last case the system is using the base classpath as root.

## Validate response http code and body together
```gherkin
Scenario:
  Then Then I ensure that the response code is 201 and the body is equal to
    """
    {
      "field": "value",
    }
    """
```

In this case, the response status code is part of the sentence, and the JSON is written directly under the sentence and enclosed in three double quotation marks.
Here it is also possible to use [JSON Unit](https://github.com/lukas-krecan/JsonUnit) syntax to validate dynamic elements.
 

## Validate response http code and body together with a JSON file
```gherkin
Scenario:
  Then I ensure that the response code is 200 and the body is equal to the file "response.json"
```

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
