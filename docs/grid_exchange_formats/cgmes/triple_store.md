# Triple store
A triplestore or RDF store is a purpose-built database for the storage and retrieval of triples through semantic queries. 
A triple is a data entity composed of subject-predicate-object such as "Generator is in France" or in RDF/XML:
```xml
<rdf:description rdf:about="generator">
    <generator:in>France</generator:in>
</rdf:description>
```

Input CGMES data read from CIM/XML files is stored natively in a purpose-specific database for RDF statements (a Triplestore). 
There are multiple open-source implementations of Triplestore engines that could be easily plugged in PowSyBl.
The only supported Triplestore engine used by PowSyBl is [RDF4J](https://rdf4j.org/).
Loading from RDF/XML files to the Triplestore is highly optimized by these engines. Furthermore, the Triplestore repository can be configured to use an in-memory store, allowing faster access to data.

## In-memory Rdf4j
[Eclipse RDF4J™](https://rdf4j.org/about/) is an open source modular Java framework for working with RDF data. This includes parsing, storing, inferencing and querying of/over such data. It offers an easy-to-use API that can be connected to all leading RDF storage solutions. It allows you to connect with SPARQL endpoints and create applications
that leverage the power of Linked Data and Semantic Web.

Its in-memory implementation is the default triplestore engine used by PowSyBl for CIM-CGMES import.
