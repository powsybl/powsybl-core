# Functional logging

```{toctree}
:hidden:
export.md
import.md
i18n.md
```

powsybl provides an API called `ReportNode` (and a default implementation called `ReportNodeImpl`).
In these functional logs we expect non-technical information useful for an end-user, about the various steps which occurred, what was unexpected, what caused an execution failure.

Each `ReportNode` contains
- several values, indexed by their keys,
- a functional message, given by a message template, which may contain references to those values or to the values of one of its ancestors,
- a list of `ReportNode` children.

Several `ReportNode` connected together through their children parameter define a tree.
Each `ReportNode` of such a tree refers to a `TreeContext`, which holds the context related to the tree.

Each message template is identified by a key.
This key is used to build a dictionary for the tree, which is carried by the `TreeContext`.
This allows to serialize in an efficient way the tree (by not repeating the duplicated templates).

When the children of a `ReportNode` is non-empty, the message of the corresponding `ReportNode` should summarize the children content.
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

The `DETAIL` severity is for end users which may want several fine-grained messages when a `WARN` or `ERROR` occurs, but do want as few `WARN` and `ERROR` messages as possible.

## Builders / adders
The builder API is accessed from a call to `ReportNode.newRootReportNode`.
It is used to build the root `ReportNode`.

The adder API is accessed from a call to `reportNode.newReportNode()`.
It is used to add a child to an existing `ReportNode`.

Both API share methods to provide the message template and the typed values:
- `withMessageTemplate(key, messageTemplate)`,
- `withUntypedValue(key, value)`,
- `withTypedValue(key, value, type)`,
- `withSeverity(severity)`.

## Example
```java
ReportNode root = ReportNode.newRootReportNode()
        .withMessageTemplate("importMessage", "Import file ${filename} in ${time} ms")
        .withTypedValue("filename", "file.txt", TypedValue.FILENAME)
        .build();
long t0 = System.currentTimeMillis();

ReportNode task1 = root.newReportNode()
        .withMessageTemplate("task1", "Doing first task with double parameter ${parameter}")
        .withUntypedValue("parameter", 4.2)
        .withSeverity(TypedValue.INFO_SEVERITY)
        .add();

ReportNode task2 = root.newReportNode()
        .withMessageTemplate("task2", "Doing second task, reading ${count} elements, among which ${problematicCount} are problematic")
        .withUntypedValue("count", 102)
        .withUntypedValue("problematicCount", 2)
        .withSeverity(TypedValue.WARN_SEVERITY)
        .add();

// Supposing a list of problematic elements has been build, each containing an id and an active power values
for (ProblematicElement element : problematicElements) {
    task2.newReportNode()
            .withMessageTemplate("problematic", "Problematic element ${id} with active power ${activePower}")
            .withTypedValue("id", element.getId(), TypedValue.ID)
            .withTypedValue("activePower", element.getActivePower(), TypedValue.ACTIVE_POWER)
            .withSeverity(TypedValue.DETAIL_SEVERITY)
            .add();
}

// Putting a value afterwards in a given reportNode
long t1 = System.currentTimeMillis();
root.getValues().put("time", t1 - t0);
```