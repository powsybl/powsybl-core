# Import

The report node JSON file obtained from `ReportNode` serialization can be deserialized
- from a file `Path`
```java
ReportNodeDeserializer.read(path);
```
- from an `InputStream`
```java
ReportNodeDeserializer.read(inputStream);
```

## Version
Currently, the serialized versions supported are the versions 2.1 and 3.0.

## Dictionaries
The two methods above look for a `default` dictionary in the `dictionaries` list.
If no `default` dictionary is found, the first entry of the list is taken.

If several dictionaries are defined in the JSON file, we can choose which dictionary has to be used for deserialization by providing the dictionary name:
```java
ReportNodeDeserializer.read(path, dictionary);
ReportNodeDeserializer.read(inputStream, dictionary);
```
Similarly, if the chosen dictionary cannot be found, the first entry of the list is taken.
