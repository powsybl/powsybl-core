module powsybl.commons.test {
    requires com.google.common;
    requires java.xml;
    requires com.google.common.jimfs; // No Automatic-Module-Name
    requires org.junit.jupiter.api;
    requires org.slf4j;
    requires org.xmlunit;

    exports com.powsybl.commons.test;
}
