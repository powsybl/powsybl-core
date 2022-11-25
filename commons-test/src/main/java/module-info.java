module powsybl.commons.test {
    requires com.google.common;
    requires java.xml;
    requires jimfs; // No Automatic-Module-Name
    requires junit;
    requires org.slf4j;
    requires org.xmlunit;

    exports com.powsybl.commons.test;
}
