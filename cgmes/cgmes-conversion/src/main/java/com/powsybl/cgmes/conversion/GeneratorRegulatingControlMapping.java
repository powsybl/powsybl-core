package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesModelException;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public class GeneratorRegulatingControlMapping {

    public GeneratorRegulatingControlMapping() {
        generator = new HashMap<>();
    }

    public void add(String generatorId, boolean regulating, String regulatingControlId) {
        if (generator.containsKey(generatorId)) {
            throw new CgmesModelException("Generator already added, Generator id : " + generatorId);
        }

        GeneratorRegulatingData rd = new GeneratorRegulatingData();
        rd.regulating = regulating;
        rd.regulatingControlId = regulatingControlId;
        generator.put(generatorId, rd);
    }

    public GeneratorRegulatingData find(String generatorId) {
        return generator.get(generatorId);
    }

    public static class GeneratorRegulatingData {
        boolean regulating;
        String regulatingControlId;
    }

    private final Map<String, GeneratorRegulatingData> generator;
}
