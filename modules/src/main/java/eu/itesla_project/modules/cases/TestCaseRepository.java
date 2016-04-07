/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.cases;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Horizon;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestCaseRepository implements CaseRepository {

    private final Path dir;

    private final String baseName;

    public TestCaseRepository() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("testcaserepo");
        dir = config.getPathProperty("dir");
        baseName = config.getStringProperty("baseName");
    }

    @Override
    public List<Network> load(DateTime date, CaseType type, Country country) {
        Network network = Importers.import_("CIM1", new GenericReadOnlyDataSource(dir, baseName), null);
        for (VoltageLevel vl : network.getVoltageLevels()) {
            vl.setHorizon(Horizon.SN);
        }
        return Collections.singletonList(network);
    }

	@Override
	public boolean isDataAvailable(DateTime date, CaseType type, Country country) {
		return isNetworkDataAvailable();
	}

	@Override
	public Map<Country, Boolean> dataAvailable(DateTime date, CaseType type, Country country) {
		Map<Country, Boolean> dataAvailable = new HashMap<Country, Boolean>();
		dataAvailable.put(country, isNetworkDataAvailable());
		return dataAvailable;
	}
	
	private boolean isNetworkDataAvailable() {
		Importer importer = Importers.getImporter("CIM1");
		DataSource ds = new GenericReadOnlyDataSource(dir, baseName);
		if (importer.exists(ds)) {
            return true;
        }
		return false;
	}

    @Override
    public Set<DateTime> dataAvailable(CaseType type, Set<Country> countries, Interval interval) {
        return Collections.emptySet();
    }
}
