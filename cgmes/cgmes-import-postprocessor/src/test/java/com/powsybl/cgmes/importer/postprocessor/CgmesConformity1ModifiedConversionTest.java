package com.powsybl.cgmes.importer.postprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;

class CgmesConformity1ModifiedConversionTest {

    @Test
    void microGridBaseCaseAssembledEntsoeCategory() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.POST_PROCESSORS, "EntsoeCategory");
        Network network = Importers.importData("CGMES", CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledEntsoeCategory().dataSource(), importParams);
        assertEquals(31, network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertEquals(42, network.getGenerator("9c3b8f97-7972-477d-9dc8-87365cc0ad0e").getExtension(GeneratorEntsoeCategory.class).getCode());
        assertNull(network.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("1dc9afba-23b5-41a0-8540-b479ed8baf4b").getExtension(GeneratorEntsoeCategory.class));
        assertNull(network.getGenerator("2844585c-0d35-488d-a449-685bcd57afbf").getExtension(GeneratorEntsoeCategory.class));
    }
}
