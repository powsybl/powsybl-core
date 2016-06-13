/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public enum TimeHorizon {
	 
		//HOUR24("24 hours", null, 1440);
		DACF("DACF", -1);
		private String name;
		private int forecastTime;
		TimeHorizon(String name, int forecastTime) {
			this.name = name;
			this.forecastTime = forecastTime;
		}
		public String getName() { return name; }
		public int getForecastTime() { return forecastTime; }
		public String getLabel() {
			String timeHorizon = name;
			if ( forecastTime > 0 ) {
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
