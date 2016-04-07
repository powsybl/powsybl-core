/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.monitoring;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusyCoresSeries implements Serializable {

    public static class Value implements Serializable {

        private final DateTime date;

        private final int busyCores;

        public Value(int busyCores) {
            this.date = DateTime.now();
            this.busyCores = busyCores;
        }

        public DateTime getDate() {
            return date;
        }

        public int getBusyCores() {
            return busyCores;
        }

    }

    private final int availableCores;

    private final List<Value> values = new ArrayList<>();

    public BusyCoresSeries(int availableCores) {
        this.availableCores = availableCores;
    }

    public int getAvailableCores() {
        return availableCores;
    }

    private void clean() {
        // clean old data
        DateTime now = DateTime.now();
        for (Iterator<Value> it = values.iterator(); it.hasNext();) {
            Value value = it.next();
            if (new Duration(value.getDate(), now).getStandardMinutes() > 1) {
                it.remove();
            }
        }
    }

    public void addValue(Value value) {
        values.add(value);
        clean();
    }

    public List<Value> getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (Iterator<Value> it = values.iterator(); it.hasNext();) {
            Value value = it.next();
            builder.append(value.getDate().toString()).append(": ").append(value.getBusyCores());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

}
