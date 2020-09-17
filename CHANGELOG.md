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
