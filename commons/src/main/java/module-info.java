module powsybl.commons {
    requires auto.service; // No Automatic-Module-Name
    requires com.fasterxml.jackson.databind; // Real module
    requires com.google.common;
    requires java.xml.bind; // Real module
    requires net.java.truevfs.comp.zip;
    requires org.apache.commons.compress;
    requires org.apache.commons.lang3;
    requires org.joda.time;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires sirocco.text.table.formatter; // No Automatic-Module-Name
    requires stax.utils; // No Automatic-Module-Name
    requires trove4j; // No Automatic-Module-Name
    requires univocity.parsers; // No Automatic-Module-Name

    exports com.powsybl.commons;
    exports com.powsybl.commons.json;
}
