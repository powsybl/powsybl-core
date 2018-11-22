package com.powsybl.commons.exceptions;

import com.powsybl.commons.PowsyblException;

public class BranchNotFoundException extends PowsyblException {

    public BranchNotFoundException() {
    }

    public BranchNotFoundException(String msg) {
        super(msg);
    }

    public BranchNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public BranchNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
