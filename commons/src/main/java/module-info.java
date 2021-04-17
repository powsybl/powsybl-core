module com.powsybl.commons {
    requires com.fasterxml.jackson.databind; // Real module
    requires com.google.common;
    requires java.xml; // Real module
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.joda.time;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires sirocco.text.table.formatter; // No Automatic-Module-Name
    requires stax.utils; // No Automatic-Module-Name
    requires trove4j; // No Automatic-Module-Name
    requires univocity.parsers; // No Automatic-Module-Name

    exports com.powsybl.commons;
    exports com.powsybl.commons.compress;
    exports com.powsybl.commons.config;
    exports com.powsybl.commons.exceptions;
    exports com.powsybl.commons.extensions;
    exports com.powsybl.commons.io.table;
    exports com.powsybl.commons.json;
    exports com.powsybl.commons.util;
    exports com.powsybl.commons.util.trove;
}
