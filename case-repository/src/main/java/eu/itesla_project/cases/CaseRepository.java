/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.cases;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface CaseRepository {

    /**
     * Load a case from the repository.
     *
     * @param date date of the case
     * @param type type of the case, snapshot (SN) or forecast (FO)
     * @param country case perimeter, merge if null
     * @return a case
     */
    List<Network> load(DateTime date, CaseType type, Country country);

    /**
     * Check if the all the data of a case is available
     *
     * @param date date of the case
     * @param type type of the case, snapshot (SN) or forecast (FO)
     * @param country case perimeter, merge if null
     * @return true if all the data of a case is available, false otherwise
     */
    boolean isDataAvailable(DateTime date, CaseType type, Country country);

    Set<DateTime> dataAvailable(CaseType type, Set<Country> countries, Interval interval);
}
