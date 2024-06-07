(timeseries)=
# Time series

## Time series modeling

In PowSyBl, time series are modeled by:
- A name to uniquely identify a time series inside a store.
- A data type which is either `double` or `String`.
- A time index to define a list of instants for which data exists. Three different implementations of the time index are available
in the framework, depending on the need:
    - Regular index: the time step size is constant
    - [Irregular index](https://en.wikipedia.org/wiki/Unevenly_spaced_time_series): the time step size varies
    - Infinite index: the time series contains only two points, one at instant 0 and another at instant `Long.MAX_VALUE`
- Metadata: a list of key/value string data
- Data chunks: an ordered list of data that will be associated to instants of the time index. The data chunks may be compressed or uncompressed.

An uncompressed JSON data chunk looks like:
```json
{
  "offset" : 0,
  "values" : [ 1.0, 1.0, 1.0, 3.0 ]
}
```
An uncompressed data chunk is modeled with a double (or String) array and an offset. 
It defines values associated to instants of the time index from `offset` to `offset + values.length`.

It is possible to compress the data chunks, using for example the [RLE](https://fr.wikipedia.org/wiki/Run-length_encoding).
The JSON serialization of compressed data chunks looks like:
Output:
```json
{
  "offset" : 0,
  "uncompressedLength" : 4,
  "stepValues" : [ 1.0, 3.0 ],
  "stepLengths" : [ 3, 1 ]
}
```

Time series can be imported from CSV data:
```java
// Creating a map of TimeSeries per version by parsing a CSV file
Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(pathToCSV);

// Creating a map of TimeSeries per version by parsing the CSV string with a specified configuration
TimeSeriesCsvConfig timeSeriesCsvConfig = new TimeSeriesCsvConfig(';', true, TimeFormat.MILLIS);
Map<Integer, List<TimeSeries>> timeSeriesPerVersion = TimeSeries.parseCsv(csvAsString, timeSeriesCsvConfig);
```

(calculated-timeseries)=
## Calculated time series

Starting from a double time series, it is possible to create calculated time series using a [Groovy](http://groovy-lang.org/)
script.

For instance, let us consider the following example.
Let's say we have created a first double time series named `dts` in a script, it is then possible 
to create new time series `a` and `b` by writing:

```groovy
ts['a'] = ts['dts'] + 1
ts['b'] = ts['a'] * 2
```
The time series `a` and `b`, serialized in JSON format, then look like:
```json
[ {
  "name" : "a",
  "expr" : {
    "binaryOp" : {
      "op" : "PLUS",
      "timeSeriesName" : "dts",
      "integer" : 1
    }
  }
}, {
  "name" : "b",
  "expr" : {
    "binaryOp" : {
      "op" : "MULTIPLY",
      "binaryOp" : {
        "op" : "PLUS",
        "timeSeriesName" : "dts",
        "integer" : 1
      },
      "integer" : 2
    }
  }
} ]
```
The calculated time series are evaluated on the fly during array conversion or iteration 
(through iterators or streams): only the arithmetic expression is stored.

Here is the list of supported vector operations:

| Operator | Purpose                                                                         | Example                 | Return type |
|----------|---------------------------------------------------------------------------------|-------------------------|-------------|
| `+`      | addition                                                                        | `ts['a'] + ts['b']`     | Numerical   |
| `-`      | substraction                                                                    | `ts['a'] - ts['b']`     | Numerical   |
| `*`      | multiplication                                                                  | `ts['a'] * ts['b']`     | Numerical   |
| `/`      | division                                                                        | `ts['a'] / ts['b']`     | Numerical   |
| `==`     | 1 if equals, 0 otherwise                                                        | `ts['a'] == ts['b']`    | Boolean     |
| `!=`     | 1 if not equals, 0 otherwise                                                    | `ts['a'] != ts['b']`    | Boolean     |
| `<`      | 1 if less than, 0 otherwise                                                     | `ts['a'] < ts['b']`     | Boolean     |
| `<=`     | 1 if less than or equals to, 0 otherwise                                        | `ts['a'] <= ts['b']`    | Boolean     |
| `>`      | 1 if greater, 0 otherwise                                                       | `ts['a'] > ts['b']`     | Boolean     |
| `>=`     | 1 if greater than or equals to, 0 otherwise                                     | `ts['a'] >= ts['b']`    | Boolean     |
| `-`      | negation                                                                        | `-ts['a']`              | Numerical   |
| `abs`    | absolute value                                                                  | `ts['a'].abs()`         | Numerical   |
| `time`   | convert to time index vector ([epoch](https://en.wikipedia.org/wiki/Unix_time)) | `ts['a'].time()`        | Numerical   |
| `min`    | min value                                                                       | `ts['a'].min(10)`       | Boolean     |
| `max`    | max value                                                                       | `ts['a'].max(10)`       | Boolean     |
| `min`    | point-to-point min values between timeseries                                    | `min(ts['a'], ts['b'])` | Numerical   |
| `max`    | point-to-point max values between timeseries                                    | `max(ts['a'], ts['b'])` | Numerical   |

In the Groovy DSL syntax, both `timeSeries['a']` and `ts['a']` are supported and are equivalent.

To compare a time index vector to a literal date, the `time('2018-01-01T00:00:01Z')` function is available. For instance, the
following code create a time series of 0 and 1 values:
```groovy
a = ts['dts'].time() < time('2018-01-01T00:00:01Z')
```

