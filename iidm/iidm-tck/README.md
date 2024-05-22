PowSyBl IIDM Technology Compatibility Kit
=========================================

This package tests IIDM implementations' compatibility with the standard.
It relies on abstract test classes, which implementors can extend in their own package to run the tests.

##### Running tck tests in your implementation

Tck tests can be run by creating an empty class extending abstract test classes:

```java
class OperationalLimitsGroupsTest extends AbstractOperationalLimitsGroupsTest {}
```

Test methods for features that you don't want to support can be overloaded to avoid failing tests.

##### How to avoid missing new tests after a release

A special tck test ensures that all other tck tests are extended :

```java
class TckSuiteExhaustivityTest extends AbstractTckSuiteExhaustivityTest {}
```

This test will fail if any other tck test is not extended in your tck test suite.

##### How about an example?

For a complete example, see [iidm-impl](../iidm-impl/src/test/java/com/powsybl/iidm/network/impl/tck).
