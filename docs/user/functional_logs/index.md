# Functional logging

```{toctree}
:hidden:
export.md
import.md
i18n.md
```

For functional logging powsybl provides an API called `ReportNode`, and a default implementation called `ReportNodeImpl`.
In these functional logs we expect non-technical information useful for an end-user, about the various steps which occurred, what was unexpected, what caused an execution failure.

Each `ReportNode` contains
- several values, indexed by their keys,
- a functional message, given by a message template referenced by a key, which may contain references to those values or to the values of one of its ancestors,
- a list of `ReportNode` children.

Several `ReportNode` connected together through their children parameter define a tree.
Each `ReportNode` of such a tree refers to the same `TreeContext`, which holds the context related to the tree.

Each message template is identified by a key.
This key corresponds to an internationalized message template: a `ResourceBundle` usually links the key to the template in the desired language (see [i18n page](./i18n.md) for more information).
A dictionary key / message template is build in the `TreeContext`: this allows to serialize in an efficient way the tree, by not repeating the duplicated templates.

When a `ReportNode` has several children, the message of the corresponding `ReportNode` should summarize the children content.
To that end, one or several values can be added after the `ReportNode` construction.
The summarizing template should be succinct: 120 characters is an indicative limit for the message string length (once formatted).

## Values
Only `float`, `double`, `int`, `float`, `boolean` and `String` values are supported in the API.

Each value can be either typed or untyped.
A typed value is a value with a `String` parameter, which gives more insight to what the value is.
This allows a GUI displaying the reports to, for instance,
- insert a hyperlink to the voltage level visualization when a voltage level is mentioned,
- insert all the parameters of the equipment which is mentioned,
- insert a link to the file mentioned,
- round some values to a given precision given their type,
- change the unit of the corresponding displayed values,
- ...

Some value types are provided by default, for instance:
- `TypedValue.TIMESTAMP`,
- `TypedValue.FILENAME`,
- `TypedValue.VOLTAGE_LEVEL`,
- `TypedValue.ACTIVE_POWER`,
- ...

## Severity
A severity is a specific typed value.
It is a `String` of type `TypedValue.SEVERITY`.
The `ReportNode` builder API gives direct access to set this value.

The following default severity values are provided:
- `TypedValue.TRACE_SEVERITY`,
- `TypedValue.DEBUG_SEVERITY`,
- `TypedValue.INFO_SEVERITY`, for reports about a functional state which may be of interest for the end user,
- `TypedValue.WARN_SEVERITY`, for reports about an unwanted state that can be recovered from,
- `TypedValue.ERROR_SEVERITY`, for reports about a requested operation that has not been completed,
- `TypedValue.DETAIL_SEVERITY`, for reports which are children of a `ReportNode` of severity `WARN` or `ERROR`.

The number of `WARN` and `ERROR` reports should be as small as possible.
That is, similar detailed reports about an unwanted state should be grouped, if possible, as children of the same `ReportNode`.
This father `ReportNode` should carry the `WARN` or `ERROR` severity, whereas these `ReportNode` children should have a `DETAIL` severity.
This allows to give fine-grained information about an unwanted state, without overwhelming the end-user with numerous `WARN` or `ERROR` reports and while keeping a succinct message for each report.

## Builders / adders
The builder API is accessed from a call to one of the `ReportNode::newRootReportNode` methods.
This API is used to build the root `ReportNode`.
The following methods are available:
- `newRootReportNode()`, to get a builder with no message template provider predefined,
- `newRootReportNode(bundleBaseName1, ...)`, to get a builder with a message template provider based on one or several `ResourceBundle`.

The following methods are available in the builder API to define the corresponding `ReportNode` tree:
- `withDefaultTimestampPattern(pattern)`, for the pattern to be used in the tree when a timestamp is added,
- `withLocale(locale)`, for specifying the `Locale` to use in the whole tree:
    - for message templates (see [i18n page](./i18n.md)),
    - for timestamps. 

The adder API is accessed from a call to `reportNode.newReportNode()`.
It is used to add a child to an existing `ReportNode`.

Both API share the following methods to provide the message template and the typed values:
- `withMessageTemplate(key)`, the key referring to a template in a `ResourceBundle` (see [i18n page](./i18n.md)), 
- `withUntypedValue(key, value)`,
- `withTypedValue(key, value, type)`,
- `withTimestamp()`, adding a typed value with node creation timestamp,
- `withSeverity(severity)`, severity being either a `String` or a `TypedValue`.

For further customization, the following methods are also available:
- `withMessageTemplateProvider(messageTemplateProvider)`, to specify how to get a message template from a given key and locale for all the descendents of the node to create, unless overridden,
- `withTimestamp(pattern)`, if a custom pattern has to be used instead of the default one specified at root construction.

## Merging ReportNodes

### Include
An `include` method is provided in the API in order to fully insert a given root `ReportNode` as a child of another `ReportNode`.
The given root `ReportNode` is becoming non-root after this operation.
This was meant for including the serialized reports obtained from another process.

### AddCopy
An `addCopy` method is provided in the API to partly insert a `ReportNode`: unlike `include`, the given node does not need to be root.
The given `ReportNode` is copied and inserted as a child of the `ReportNode`.

Two known limitations of this method:
1. the inherited values of copied `ReportNode` are not kept,
2. the resulting dictionary contains all the keys from the copied `ReportNode` tree, even the ones from non-copied `ReportNode`s.

## Example

Resource bundle property file in `com/powsybl/commons/reports.properties` which is the default translation values file.
```properties
translationKey = Import file ${filename} in ${time} ms
task1 = Doing first task with double parameter ${parameter}
task2 = Doing second task, reading ${count} elements, among which ${problematicCount} are problematic
problematic = Problematic element ${id} with active power ${activePower}
```

```java
String bundleName = "com.powsybl.commons.reports";

ReportNode root = ReportNode.newRootReportNode()
        .withLocaleMessageTemplate("translationKey", bundleName)
        .withTypedValue("filename", "file.txt", TypedValue.FILENAME)
        .build();
long t0 = System.currentTimeMillis();

ReportNode task1 = root.newReportNode()
        .withLocaleMessageTemplate("task1", bundleName)
        .withUntypedValue("parameter", 4.2)
        .withSeverity(TypedValue.INFO_SEVERITY)
        .add();

ReportNode task2 = root.newReportNode()
        .withLocaleMessageTemplate("task2", bundleName)
        .withUntypedValue("count", 102)
        .withUntypedValue("problematicCount", 2)
        .withSeverity(TypedValue.WARN_SEVERITY)
        .add();

// Supposing a list of problematic elements has been build, each containing an id and an active power values
for (ProblematicElement element : problematicElements) {
    task2.newReportNode()
            .withLocaleMessageTemplate("problematic", bundleName)
            .withTypedValue("id", element.getId(), TypedValue.ID)
            .withTypedValue("activePower", element.getActivePower(), TypedValue.ACTIVE_POWER)
            .withSeverity(TypedValue.DETAIL_SEVERITY)
            .add();
}

// Putting a value afterward in a given reportNode
long t1 = System.currentTimeMillis();
root.addTypedValue("time", t1 - t0, "ELAPSED_TIME");
```