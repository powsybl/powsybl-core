package com.powsybl.security.json.action;

import com.powsybl.security.action.DanglingLineAction;

public class DanglingLineActionSerializer extends AbstractLoadActionSerializer<DanglingLineAction> {
    public DanglingLineActionSerializer() {
        super(DanglingLineAction.class);
    }
}
