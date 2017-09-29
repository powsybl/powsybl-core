/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.commons.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.commons.datasource.ReadOnlyDataSourceFactory;
import eu.itesla_project.iidm.import_.Importers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Common ENTSOE and IIDM case repository layout:
 * <pre>
 * CIM/SN/2013/01/15/20130115_0620_SN2_FR0.zip
 *    /FO/...
 * UCT/SN/...
 *    /FO/...
 * IIDM/SN/2013/01/15/20130115_0720_SN2_FR0.xml
 *    /FO/...
 * </pre>
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EntsoeAndXmlCaseRepository extends EntsoeCaseRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntsoeAndXmlCaseRepository.class);

    public EntsoeAndXmlCaseRepository(EntsoeCaseRepositoryConfig config, ComputationManager computationManager) {
        super(config,
            Arrays.asList(new EntsoeFormat(Importers.getImporter("CIM1", computationManager), "CIM"),
                          new EntsoeFormat(Importers.getImporter("UCTE", computationManager), "UCT"), // official ENTSOE formats)
                          new EntsoeFormat(Importers.getImporter("XIIDM", computationManager), "IIDM")), // XIIDM format
            (directory, baseName) -> new GenericReadOnlyDataSource(directory, baseName));
    }

    public EntsoeAndXmlCaseRepository(EntsoeCaseRepositoryConfig config, List<EntsoeFormat> formats, ReadOnlyDataSourceFactory dataSourceFactory) {
        super(config, formats, dataSourceFactory);
    }

    public static CaseRepository create(ComputationManager computationManager) {
        return new EntsoeAndXmlCaseRepository(EntsoeAndXmlCaseRepositoryConfig.load(), computationManager);
    }

}
