# Getting started with time series

```xml
<dependency>
  <groupId>com.powsybl</groupId>
  <artifactId>powsybl-time-series-api</artifactId>
  <version>#VERSION#</version>
</dependency>
```

## Time series modeling

In PowSyBl time series are modelled by 

 - A name to uniquely identify the time series inside a store.
 - A type: double or string.
 - A time index to define an instant list to which data exists for this time series. Three differents implementation of time index are available in the framework depending of the need:
    - Regular index
    - Irregular index
    - Infinite index
 - Metadata: a list of key/value string data
 - Data chunks: an ordered list of data that will be associated to instant of the time index.

## Time index

### 1.  Irregular

To create an irregular time series index with 3 instants:

```javascript
Instant t1 = Instant.parse("2018-01-01T00:00:00Z");
Instant t2 = t1.plusSeconds(1);
Instant t3 = t2.plusSeconds(2);
TimeSeriesIndex index = IrregularTimeSeriesIndex.create(t1, t2, t3);
```
### 2.  Regular

To create a regular time series with 3 instants equally spaced of 1 second:

```javascript
Instant start = Instant.parse("2018-01-01T00:00:00Z");
Instant end = start.plusSeconds(2);
Duration spacing = Duration.ofSeconds(1);
TimeSeriesIndex index2 = RegularTimeSeriesIndex.create(start, end, spacing);
```

### 3.  Infinite

TODO

## Create a double time series

To create a double format time series based on time index `index`:

```java
DoubleTimeSeries a = TimeSeries.create("a", index);
```

We now have a time series with 3 instants but without any data.  By default the time series is filled with NaN values which means absent value.

```java
double[] values = c.toArray();
System.out.println(Arrays.toString(values));
```

Output:

```
[NaN, NaN, NaN]
```

In order to add data to the time series, we need to create data chunks: double data chunks for double time series and string data chunks for string time series.

## Create a double array chunk

To create an uncompress data chunk and print its json representation:

```java
DoubleArrayChunk chunk = ArrayChunk.create(1d, 1d, 1d, 3d);
System.out.println(chunk.toJson());
```

Output:
```json
{
  "offset" : 0,
  "values" : [ 1.0, 1.0, 1.0, 3.0 ]
}
```

We can see that an uncompress data chunk is juste a double array and an offset. It defines values associated to instant of the time index from offset to offset + values.length.

To compress the chunk using RLE compression (https://fr.wikipedia.org/wiki/Run-length_encoding)

```java
DoubleArrayChunk compressedChunk = chunk.tryToCompress();
System.out.println(compressedChunk);
```

Output:
```json
{
  "offset" : 0,
  "uncompressedLength" : 4,
  "stepValues" : [ 1.0, 3.0 ],
  "stepLengths" : [ 3, 1 ]
}
```

`chunk.tryToCompress()`compute a compression factor by estimating the uncompressed and compressed data size of the data chunk. If compression factor is greater or equals to one, it returns itselfs otherwise it returns the compressed data chunk.

Compression factor could be accessed like this:

```java
System.out.println(compressedChunk.getCompressionFactor());
```

Output:

```
0.75
```

So here size of compressed data chunk is 0.75 smaller than uncompressed one.

## Create a string array chunk

```java
StringArrayChunk chunk2 = ArrayChunk.create("hello", "bye", "bye", "bye");
System.out.println(chunk2.toJson());
System.out.println(chunk2.tryToCompress().toJson());
```

Output:

```
{
  "offset" : 0,
  "values" : [ "hello", "bye", "bye", "bye" ]
}
{
  "offset" : 0,
  "uncompressedLength" : 4,
  "stepValues" : [ "hello", "bye" ],
  "stepLengths" : [ 1, 3 ]
}
```



## Create a double calculated time series

```java
List<DoubleTimeSeries> result = DoubleTimeSeries.fromTimeSeries(a)
                                                .build("ts['b'] = ts['a'] + 1",
                                                       "ts['c'] = ts['b'] * 2");
System.out.println(TimeSeries.toJson(result));
System.out.println(Arrays.toString(result.get(0).toArray()));
System.out.println(Arrays.toString(result.get(1).toArray()));
```

```
[ {
  "name" : "b",
  "expr" : {
    "binaryOp" : {
      "op" : "PLUS",
      "timeSeriesName" : "a",
      "integer" : 1
    }
  }
}, {
  "name" : "c",
  "expr" : {
    "binaryOp" : {
      "op" : "MULTIPLY",
      "binaryOp" : {
        "op" : "PLUS",
        "timeSeriesName" : "a",
        "integer" : 1
      },
      "integer" : 2
    }
  }
} ]
[2.0, 3.0]
[4.0, 6.0]
```



