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
- Data chunks: an ordered list of data that will be associated with instants of the time index. The data chunks may be compressed or uncompressed.

An uncompressed JSON data chunk looks like:
```json
{
  "offset" : 0,
  "values" : [ 1.0, 1.0, 1.0, 3.0 ]
}
```
An uncompressed data chunk is modeled with a double (or String) array and an offset.
It defines values associated to instants of the time index from `offset` to `offset + values.length`.

It is possible to compress the data chunks, using, for example, the [RLE](https://en.wikipedia.org/wiki/Run-length_encoding).
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

## Splitting time series

A time series can be split (as in re-arranged) into time series of at most `k` points, using `timeSeries.split(k)` (or
`TimeSeries.split(timeSeriesList, k)` to split a list of time series sharing the same index; each of them must then have
data in every window, otherwise a `TimeSeriesException` is thrown). A `k` lower than 1 is rejected with an
`IllegalArgumentException`.
- the index is cut into windows aligned on the absolute grid `[j * k, (j + 1) * k[`, where `j` is the window number (`j = 0, 1, 2...`);
- chunks crossing a window boundary are cut, and chunks lying in the same window are merged
- one time series, holding a single data chunk, is created for each window containing data. Windows without data are skipped;
- inside a window, the chunk starts at the first index and ends at the last index covered by data: there is no filling
  at the edges of the window. If the window contains several parts of chunks separated by indexes without data, they are
  merged into one uncompressed chunk where the missing values are `NaN` (double) or `null` (String), as in `toArray()`;
- the kind of the chunks is preserved: a compressed chunk gives compressed chunks, successive compressed chunks falling
  in the same window are merged into a compressed chunk. Chunks of different kinds falling in the same window are merged
  into an uncompressed chunk. A chunk lying entirely in a window is reused as is.

The resulting time series are therefore not necessarily successive and do not necessarily start or end on the window
bounds: users shall rely on the chunk offsets. The split is done in a single pass over the chunks, in linear time.

For instance, with a time series of 10 points holding a chunk of offset 0 and length 3 and a chunk of offset 6 and
length 4, `split(4)` gives three time series holding respectively the points `[0, 3[`, `[6, 8[` and `[8, 10[`.

Data chunks themselves can be cut with `DataChunk.slice(from, to)`, which returns the part of the chunk lying in the
index range `[from, to[`, with the same kind (compressed or not) as the original chunk.

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

```{warning}
Groovy scripts are executed without any sandbox. Only use scripts from trusted sources. See [Groovy scripts security](../user/groovy-security.md).
```

To compare a time index vector to a literal date, the `time('2018-01-01T00:00:01Z')` function is available. For instance, the
following code creates a time series of 0 and 1 values:
```groovy
a = ts['dts'].time() < time('2018-01-01T00:00:01Z')
```

