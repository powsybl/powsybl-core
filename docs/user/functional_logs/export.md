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

An example of the current version 2.1 of the serialization is below:
```json
{
  "version" : "2.1",
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
In order to have an overview of a report node, two print methods are provided in the API:
- for printing to a `Path`
```java
reportNode.print(path);
```
- for printing to a `Writer`
```java
reportNode.print(writer);
```

The correspond multiline string of above example is below.
The `+` character and the indentation are used to show the tree hierarchy.
```text
+ template with typed value
   template with several untyped first untyped value, second untyped value
```