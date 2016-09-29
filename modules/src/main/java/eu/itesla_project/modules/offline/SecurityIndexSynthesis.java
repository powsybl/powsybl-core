/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityIndexSynthesis implements Serializable {

    public static class SecurityBalance implements Serializable {
        
        private int stableCount;

        private int unstableCount;

        public SecurityBalance(int stableCount, int unstableCount) {
            this.stableCount = stableCount;
            this.unstableCount = unstableCount;
        }

        public SecurityBalance() {
            this(0, 0);
        }

        public int getStableCount() {
            return stableCount;
        }

        public int getUnstableCount() {
            return unstableCount;
        }

    }
    
    private static final SecurityBalance ZERO = new SecurityBalance();
    
    private final Table<String, SecurityIndexType, SecurityBalance> table = HashBasedTable.create();
    
    public SecurityIndexSynthesis() {
    }

    public Set<String> getContingencyIds() {
        return table.rowKeySet();
    }
    
    public Set<SecurityIndexType> getSecurityIndexTypes() {
        return table.columnKeySet();
    }
    
    public void addSecurityIndex(SecurityIndexId securityIndexId, boolean stable) {
        SecurityBalance balance = table.get(securityIndexId.getContingencyId(), securityIndexId.getSecurityIndexType());
        if (balance == null) {
            balance = new SecurityBalance();
            table.put(securityIndexId.getContingencyId(), securityIndexId.getSecurityIndexType(), balance);
        }
        if (stable) {
            balance.stableCount++;
        } else {
            balance.unstableCount++;
        }
    }
    
    public void addSecurityIndex(SecurityIndex securityIndex) {
        addSecurityIndex(securityIndex.getId(), securityIndex.isOk());
    }
    
    public SecurityBalance getSecurityBalance(String contingencyId, SecurityIndexType securityIndexType) {
        SecurityBalance balance = table.get(contingencyId, securityIndexType);
        if (balance == null) {
            return ZERO;
        } else {
            return balance;
        }
    }

}
