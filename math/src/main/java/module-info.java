module com.powsybl.math {
    requires com.google.common;
    requires com.powsybl.commons;
    requires jama; // No Automatic-Module-Name
    requires org.slf4j;
    requires org.scijava.nativelib;

    exports com.powsybl.math.graph;
    exports com.powsybl.math.matrix;
}
