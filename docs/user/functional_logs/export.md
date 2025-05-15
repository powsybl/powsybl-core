# Export
A report node can be either serialized to a JSON file or printed to a string / a text file.

## JSON format
A report node can be serialized into a JSON file
- either by calling
```java
var objectMapper = new ObjectMapper().registerModule(new ReportNodeJsonModule());
objectMapper.writeValue(outputStream, reportNode);
```
- or by calling 
```java
ReportNodeSerializer.write(reportNode, jsonFilePath);
```

An example of the current version 3.0 of the serialization is below:
```json
{
  "version" : "3.0",
  "dictionaries" : {
    "default" : {
      "key1" : "template with typed ${value1}",
      "key2" : "template with several untyped ${value1}, ${value2}"
    }
  },
  "reportRoot" : {
    "messageKey" : "key1",
    "values" : {
      "value1" : {
        "value" : "value",
        "type" : "FILENAME"
      }
    },
    "children" : [ {
      "messageKey" : "key2",
      "values" : {
        "value1" : {
          "value" : "first untyped value"
        },
        "value2" : {
          "value" : "second untyped value"
        }
      }
    } ]
  }
}
```

## Display format
To get an overview of a report node, several print methods are provided in the API, with the possibility to provide your own `Formatter` -
`Formatter` is a functional interface that specifies how to get a `String` from a `TypedValue`.
- To print to a `Path`:
```java
reportNode.print(path);
reportNode.print(path, formatter);
```
- To print to a `Writer`:
```java
reportNode.print(writer);
reportNode.print(writer, formatter);
```

In both cases, giving a custom formatter allows to do specific formatting based on types for instance.
If no formatter is provided, the default one is used:
```java
typedValue -> typedValue.getValue().toString()
```

The corresponding multiline string of above example is below.
The `+` character and the indentation are used to show the tree hierarchy.
```text
+ template with typed value
   template with several untyped first untyped value, second untyped value
```