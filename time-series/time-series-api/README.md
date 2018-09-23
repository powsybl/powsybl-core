# Getting started with time series

```xml
<dependency>
  <groupId>com.powsybl</groupId>
  <artifactId>powsybl-time-series-api</artifactId>
  <version>#VERSION#</version>
</dependency>
```

## Create a time index

```java
Instant t1 = Instant.now();
Instant t2 = t1.plusSeconds(1);
TimeSeriesIndex index = IrregularTimeSeriesIndex.create(t1, t2);
```
## Create a double time series
```java
DoubleTimeSeries a = TimeSeries.create("a", index, 1, 2);
```

## Create a double array chunk
```java
DoubleArrayChunk chunk = ArrayChunk.create(1d, 1d, 1d, 3d);
System.out.println(chunk.toJson());
System.out.println(chunk.tryToCompress().toJson());
```

```
{
  "offset" : 0,
  "values" : [ 1.0, 1.0, 1.0, 3.0 ]
}
{
  "offset" : 0,
  "uncompressedLength" : 4,
  "stepValues" : [ 1.0, 3.0 ],
  "stepLengths" : [ 3, 1 ]
}
```

## Create a string array chunk
```java
StringArrayChunk chunk2 = ArrayChunk.create("hello", "bye", "bye", "bye");
System.out.println(chunk2.toJson());
System.out.println(chunk2.tryToCompress().toJson());
```

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



