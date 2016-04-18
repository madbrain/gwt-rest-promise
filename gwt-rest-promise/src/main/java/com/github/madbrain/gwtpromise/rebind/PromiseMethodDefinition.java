package com.github.madbrain.gwtpromise.rebind;

import com.gwtplatform.dispatch.rest.rebind.Parameter;

import java.util.List;

public class PromiseMethodDefinition {
    private String name;
    private String returnType;
    private List<Parameter> parameters;

    public PromiseMethodDefinition(String name, String returnType, List<Parameter> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}
