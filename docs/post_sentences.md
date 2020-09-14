# Sentences for POST requests

- [When](#when)
    - [Execute a POST request to an endpoint with previously given URI and body](#execute-a-post-request-to-an-endpoint-with-previously-given-uri-and-body)
    - [Execute an authorized POST request call to an endpoint with previously given URI and body](#execute-an-authorized-post-request-call-to-an-endpoint-with-previously-given-uri-and-body)
    - [Execute a POST request call to a URL with a previously given body](#execute-a-post-request-call-to-a-url-with-a-previously-given-body)
    - [Execute an authorized POST request call to a URL with a previously given body](#execute-an-authorized-post-request-call-to-a-url-with-a-previously-given-body)
    - [Execute a POST request call to an endpoint with body from file](#execute-a-post-request-call-to-an-endpoint-with-body-from-file)
    - [Execute an authorized POST request call to an endpoint with body from file](#execute-an-authorized-post-request-call-to-an-endpoint-with-body-from-file)
    - [Execute an authorized POST request call to previously given URL and body with dynamic URI elements](#execute-an-authorized-post-request-call-to-previously-given-url-and-body-with-dynamic-uri-elements)
    - [Execute a POST request call to previously given URL and body with dynamic URI elements](#execute-a-post-request-call-to-previously-given-url-and-body-with-dynamic-uri-elements)


# When
The paths that are used here can be shortened by set a base URL path with [Set base path for URLs](#set-base-path-for-urls) with a `Given` Step before.


## Execute a POST request to an endpoint with previously given URI and body
```gherkin
Scenario:
  When executing a POST call with previously given URI and body
```

Calls a previously given URI path as a `POST` request without `Authorization` header, and a previously given body.

## Execute an authorized POST request call to an endpoint with previously given URI and body
```gherkin
Scenario:
  When executing an authorized POST call with previously given URI and body
```

Calls a previously given URL path as a `POST` request with `Authorization` header, and a previously given body.

## Execute a POST request call to a URL with a previously given body
```gherkin
Scenario:
  When executing a POST call to {string} with previously given body
```

Calls the given URL path with a previously given Body as a `POST` request.

## Execute an authorized POST request call to a URL with a previously given body
```gherkin
Scenario:
  When executing an authorized POST call to {string} with previously given body
```

Calls the given URL path with a previously given Body as a `POST` request.

The used token depends on [Define that a token without scopes should be used](#define-that-a-token-without-scopes-should-be-used) `Step`.


## Execute a POST request call to an endpoint with body from file
```gherkin
Scenario:
  When executing a POST call to {string} with the body from file {string}
```

Calls the given URL path as a `POST` request without `Authorization` header, and a body defined in the given file.

## Execute an authorized POST request call to an endpoint with body from file
```gherkin
Scenario:
  When executing an authorized POST call to {string} with the body from file {string}
```

Calls the given URL path as a `POST` request with `Authorization` header, and a body defined in the given file.
The used token depends on [Define that a token without scopes should be used](#define-that-a-token-without-scopes-should-be-used) `Step`.

## Execute an authorized POST request call to previously given URL and body with dynamic URI elements
```gherkin
Scenario:
  When executing an authorized POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
    | URI Elements  | URI Values |
    | resourceId    | abc-def-gh |
    | subResourceId | abc-def-gh |
```

Calls a previously given URL path and Body as a `POST` request and replace dynamic URI elements with values.

In the example above the given link looks like: `/api/v1/endpoint/{resourceId}/{subResourceId}`.
The dynamic elements `{resourceId}` and `{subResourceId}` will be replaced with the values from the datatable below the sentence.

This datatable requires the header `| URI Elements  | URI Values |`.

The values do first a lookup in the `ScenarioStateContext`, if there is a key equal to the `URI Values` value.
If it finds the key, this key will be used, else it uses the value of the table directly.

To set something to the `ScenarioStateContext` it is possible to use [Read from Response and set it to a Feature context](#read-from-response-and-set-it-to-a-feature-context).
But like mentioned at this point, this is an [Anti-Pattern](https://cucumber.io/docs/guides/anti-patterns/), which should be used with caution.

**Requires the `Steps` [Set a URI path for later execution](#set-a-uri-path-for-later-execution) and [Set a body from JSON file for later execution](#set-a-body-from-json-file-for-later-execution)!**
The used token depends on [Define that a token without scopes should be used](#define-that-a-token-without-scopes-should-be-used) `Step`.

## Execute a POST request call to previously given URL and body with dynamic URI elements
```gherkin
Scenario:
  When executing a POST call with previously given API path, body and these dynamic 'URI Elements' replaced with the 'URI Values'
    | URI Elements  | URI Values |
    | resourceId    | abc-def-gh |
    | subResourceId | abc-def-gh |
```

Calls a previously given URL path and Body as a `POST` request and replace dynamic URI elements with values.

In the example above the given link looks like: `/api/v1/endpoint/{resourceId}/{subResourceId}`.
The dynamic elements `{resourceId}` and `{subResourceId}` will be replaced with the values from the datatable below the sentence.

This datatable requires the header `| URI Elements  | URI Values |`.

The values do first a lookup in the `ScenarioStateContext`, if there is a key equal to the `URI Values` value.
If it finds the key, this key will be used, else it uses the value of the table directly.

To set something to the `ScenarioStateContext` it is possible to use [Read from Response and set it to a Feature context](#read-from-response-and-set-it-to-a-feature-context).
But like mentioned at this point, this is an [Anti-Pattern](https://cucumber.io/docs/guides/anti-patterns/), which should be used with caution.

**Requires the `Steps` [Set a URI path for later execution](#set-a-uri-path-for-later-execution) and [Set a body from JSON file for later execution](#set-a-body-from-json-file-for-later-execution)!**
