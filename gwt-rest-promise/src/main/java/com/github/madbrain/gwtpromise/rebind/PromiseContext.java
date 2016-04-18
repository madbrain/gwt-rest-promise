package com.github.madbrain.gwtpromise.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;

public class PromiseContext {

    private final JClassType promiseType;

    public PromiseContext(JClassType type) {
        this.promiseType = type;
    }

    public JClassType getPromiseType() {
        return promiseType;
    }
}
