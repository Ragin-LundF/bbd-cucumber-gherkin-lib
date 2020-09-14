# Sentences for GET requests

- [When](#when)
    - [Execute a GET request call with previously given URI](#execute-a-get-request-call-with-previously-given-uri)
    - [Execute an authorized GET request call with previously given URI](#execute-an-authorized-get-request-call-with-previously-given-uri)
    - [Execute a GET request call to an endpoint](#execute-a-get-request-call-to-an-endpoint)
    - [Execute an authorized GET request call](#execute-an-authorized-get-request-call)
    - [Execute an authorized GET request call to previously given URL with dynamic URI elements](#execute-an-authorized-get-request-call-to-previously-given-url-with-dynamic-uri-elements)
    - [Execute a GET request call to previously given URL with dynamic URI elements](#execute-a-get-request-call-to-previously-given-url-with-dynamic-uri-elements)


# When
The paths that are used here can be shortened by set a base URL path with [Set base path for URLs](#set-base-path-for-urls) with a `Given` Step before.

## Execute a GET request call with previously given URI
```gherkin
Scenario:
  When executing a GET call with previously given URI
```

Calls a previously given URL path as a `GET` request without `Authorization` header.

## Execute an authorized GET request call with previously given URI
```gherkin
Scenario:
  When executing an authorized GET call with previously given URI
```

Calls a previously given URL path as a `GET` request with `Authorization` header.

## Execute a GET request call to an endpoint
```gherkin
Scenario:
  When executing a GET call to {string}
```

Calls the given URL path as a `GET` request without `Authorization` header.

## Execute an authorized GET request call
```gherkin
Scenario:
  When executing an authorized GET call to {string}
```

Calls the given URL path as a `GET` request with `Authorization` header and `Bearer` token.
The used token depends on [Define that a token without scopes should be used](#define-that-a-token-without-scopes-should-be-used) `Step`.


## Execute an authorized GET request call to previously given URL with dynamic URI elements
```gherkin
Scenario:
  When executing an authorized GET call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'
    | URI Elements  | URI Values |
    | resourceId    | abc-def-gh |
    | subResourceId | abc-def-gh |
```

Calls a previously given URL path as a `GET` request and replace dynamic URI elements with values.

In the example above the given link looks like: `/api/v1/endpoint/{resourceId}/{subResourceId}`.
The dynamic elements `{resourceId}` and `{subResourceId}` will be replaced with the values from the datatable below the sentence.

This datatable requires the header `| URI Elements  | URI Values |`.

The values do first a lookup in the `ScenarioStateContext`, if there is a key equal to the `URI Values` value.
If it finds the key, this key will be used, else it uses the value of the table directly.

To set something to the `ScenarioStateContext` it is possible to use [Read from Response and set it to a Feature context](#read-from-response-and-set-it-to-a-feature-context).
But like mentioned at this point, this is an [Anti-Pattern](https://cucumber.io/docs/guides/anti-patterns/), which should be used with caution.

**Requires the `Step` [Set a URI path for later execution](#set-a-uri-path-for-later-execution)!**
The used token depends on [Define that a token without scopes should be used](#define-that-a-token-without-scopes-should-be-used) `Step`.


## Execute a GET request call to previously given URL with dynamic URI elements
```gherkin
Scenario:
  When executing a GET call with previously given API path and these dynamic 'URI Elements' replaced with the 'URI Values'
    | URI Elements  | URI Values |
    | resourceId    | abc-def-gh |
    | subResourceId | abc-def-gh |
```

Calls a previously given URL path as a `GET` request and replace dynamic URI elements with values.

In the example above the given link looks like: `/api/v1/endpoint/{resourceId}/{subResourceId}`.
The dynamic elements `{resourceId}` and `{subResourceId}` will be replaced with the values from the datatable below the sentence.

This datatable requires the header `| URI Elements  | URI Values |`.

The values do first a lookup in the `ScenarioStateContext`, if there is a key equal to the `URI Values` value.
If it finds the key, this key will be used, else it uses the value of the table directly.

To set something to the `ScenarioStateContext` it is possible to use [Read from Response and set it to a Feature context](#read-from-response-and-set-it-to-a-feature-context).
But like mentioned at this point, this is an [Anti-Pattern](https://cucumber.io/docs/guides/anti-patterns/), which should be used with caution.

**Requires the `Step` [Set a URI path for later execution](#set-a-uri-path-for-later-execution)!**

