# Advanced JSON Validator configuration

- [Configure the JSON compare to ignore extra elements in arrays](#configure-the-json-compare-to-ignore-extra-elements-in-arrays)
- [Configure the JSON compare to ignore the order of arrays](#configure-the-json-compare-to-ignore-the-order-of-arrays)
- [Configure the JSON compare to ignore extra fields](#configure-the-json-compare-to-ignore-extra-fields)


## Configure the JSON compare to ignore extra elements in arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays with extra elements
```

_It is also possible to use the `@bdd_lib_json_ignore_new_array_elements` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore new array elements.

See [src/test/resources/features/flexible_json/](../src/test/resources/features/flexible_json/) for examples.


## Configure the JSON compare to ignore the order of arrays
```gherkin
Scenario:
  Given that the response JSON can contain arrays in a different order
```

_It is also possible to use the `@bdd_lib_json_ignore_array_order` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore the order of arrays.

See [src/test/resources/features/flexible_json/](../src/test/resources/features/flexible_json/) for examples.


## Configure the JSON compare to ignore extra fields
```gherkin
Scenario:
  Given that the response JSON can contain extra fields
```

_It is also possible to use the `@bdd_lib_json_ignore_extra_fields` annotation on `Feature` or `Scenario` level._

With this sentence or annotation, the JSON comparison will ignore new/not defined fields in the response.

See [src/test/resources/features/flexible_json/](../src/test/resources/features/flexible_json/) for examples.
