package com.github.madbrain.gwtpromise.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rest.rebind.AbstractGenerator;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionContext;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionGenerator;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionPoint;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceContext;
import com.gwtplatform.dispatch.rest.rebind.resource.ResourceDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.ClassDefinition;
import com.gwtplatform.dispatch.rest.rebind.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PromiseResourceExtensionGenerator extends AbstractGenerator implements ExtensionGenerator {

    private PromiseResourceGenerator promiseResourceGenerator;

    @Inject
    public PromiseResourceExtensionGenerator(Logger logger, GeneratorContext context, PromiseResourceGenerator promiseResourceGenerator) {
        super(logger, context);
        this.promiseResourceGenerator = promiseResourceGenerator;
    }

    @Override
    public boolean canGenerate(ExtensionContext context) {
        return context.getExtensionPoint() == ExtensionPoint.AFTER_RESOURCES;
    }

    @Override
    public Collection<ClassDefinition> generate(ExtensionContext context) throws UnableToCompleteException {
        List<ClassDefinition> definitions = new ArrayList<>();
        for (JClassType type : getContext().getTypeOracle().getTypes()) {
            PromiseContext promiseContext = new PromiseContext(type);
            if (promiseResourceGenerator.canGenerate(promiseContext)) {
                definitions.add(promiseResourceGenerator.generate(promiseContext));
            }
        }
        return definitions;
    }

}
