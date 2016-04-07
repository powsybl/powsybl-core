/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import eu.itesla_project.iidm.network.Horizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public enum TimeHorizon {
	 
		//HOUR24("24 hours", null, 1440);
		DACF("DACF", Horizon.DACF, -1);
		private String name;
		private Horizon horizon;
		private int forecastTime;
		TimeHorizon(String name, Horizon horizon, int forecastTime) {
			this.name = name;
			this.horizon = horizon;
			this.forecastTime = forecastTime;
		}
		public String getName() { return name; }
		public Horizon getHorizon() { return horizon; }
		public int getForecastTime() { return forecastTime; }
		public String getLabel() {
			String timeHorizon = "";
			if ( horizon != null )
				timeHorizon += horizon;
			if ( forecastTime > 0 ) {
				if ( horizon != null )
					timeHorizon += "-";
				timeHorizon += forecastTime;
			}
			return timeHorizon;
		}

        public static TimeHorizon fromName(String name) {
            for (TimeHorizon timeHorizon : TimeHorizon.values()) {
                if ( timeHorizon.name.equals(name)) {
                    return timeHorizon;
                }
            }
            throw new IllegalArgumentException("No time horizon found with label " + name);
        }

}
