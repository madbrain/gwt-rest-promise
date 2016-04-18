package com.github.madbrain.gwtpromise.rebind;

import com.github.madbrain.gwtpromise.client.Promise;
import com.github.madbrain.gwtpromise.client.rpc.AsyncCallPromise;
import com.github.madbrain.gwtpromise.client.rpc.PromiseOf;
import com.github.madbrain.gwtpromise.client.rpc.ResourcePromiseFactory;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.rest.delegates.client.ResourceDelegate;
import com.gwtplatform.dispatch.rest.rebind.AbstractVelocityGenerator;
import com.gwtplatform.dispatch.rest.rebind.GeneratorWithInput;
import com.gwtplatform.dispatch.rest.rebind.Parameter;
import com.gwtplatform.dispatch.rest.rebind.events.RegisterGinBindingEvent;
import com.gwtplatform.dispatch.rest.rebind.utils.*;
import com.gwtplatform.dispatch.rest.rebind.utils.Arrays;
import org.apache.velocity.app.VelocityEngine;

import java.io.PrintWriter;
import java.util.*;

import static com.gwtplatform.dispatch.rest.rebind.utils.JPrimitives.classTypeOrConvertToBoxed;

public class PromiseResourceGenerator extends AbstractVelocityGenerator
        implements GeneratorWithInput<PromiseContext, PromiseResourceDefinition> {

    private static final String TEMPLATE = "com/github/madbrain/gwtpromise/rebind/PromiseResource.vm";

    private final EventBus eventBus;

    private List<ClassDefinition> generatedDelegates = new ArrayList<>();
    private Set<String> imports;
    private PromiseContext promiseContext;
    private JClassType resourceType;
    private List<PromiseMethodDefinition> methodDefinitions;

    @Inject
    public PromiseResourceGenerator(Logger logger, GeneratorContext context,
                                    VelocityEngine velocityEngine, EventBus eventBus) {
        super(logger, context, velocityEngine);
        this.eventBus = eventBus;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getPackageName() {
        return promiseContext.getPromiseType().getPackage().getName();
    }

    @Override
    protected String getImplName() {
        return promiseContext.getPromiseType().getSimpleSourceName() + "Factory";
    }

    @Override
    public boolean canGenerate(PromiseContext promiseContext) {
        this.promiseContext = promiseContext;
        return promiseContext.getPromiseType().isInterface() != null
                && getPromiseOf(promiseContext.getPromiseType()) != null
                && !generatedDelegates.contains(getClassDefinition());
    }

    private JClassType getPromiseOf(JClassType type) {
        try {
            JClassType promiseOf = getType(PromiseOf.class);
            for (JClassType implementedInterface : type.getImplementedInterfaces()) {
                if (implementedInterface.isAssignableTo(promiseOf)) {
                    return implementedInterface;
                }
            }
        } catch (UnableToCompleteException e) {
        }
        return null;
    }

    @Override
    public PromiseResourceDefinition generate(PromiseContext promiseContext) throws UnableToCompleteException {
        this.promiseContext = promiseContext;
        JClassType promiseType = promiseContext.getPromiseType();
        JClassType promiseOf = getPromiseOf(promiseType);
        resourceType = promiseOf.isParameterized().getTypeArgs()[0];

        imports = new TreeSet<String>();
        imports.add(AsyncCallPromise.class.getName());
        imports.add(Promise.class.getName());
        imports.add(ResourceDelegate.class.getName());
        imports.add(ResourcePromiseFactory.class.getName());
        imports.add(resourceType.getQualifiedSourceName());

        methodDefinitions = new ArrayList<>();

        generateMethods();

        PrintWriter printWriter = tryCreate();
        mergeTemplate(printWriter);
        commit(printWriter);

        registerPromiseFactoryBinding();

        PromiseResourceDefinition definition = new PromiseResourceDefinition(getPackageName(), getImplName());
        generatedDelegates.add(definition);
        return definition;
    }

    private void generateMethods() throws UnableToCompleteException {
        List<JMethod> methods = com.gwtplatform.dispatch.rest.rebind.utils.Arrays.asList(resourceType.getInheritableMethods());

        for (JMethod enclosedMethod : methods) {
            generateMethod(enclosedMethod);
        }
    }

    private void generateMethod(JMethod method) throws UnableToCompleteException {
        JClassType resultType = classTypeOrConvertToBoxed(getContext().getTypeOracle(), method.getReturnType());
        List<Parameter> parameters = resolveParameters(method);
        PromiseMethodDefinition delegatedDefinition = new PromiseMethodDefinition(
                method.getName(), resultType.getParameterizedQualifiedSourceName(), parameters);
        methodDefinitions.add(delegatedDefinition);
    }

    protected List<Parameter> resolveParameters(JMethod method) {
        List<JParameter> jParameters = Arrays.asList(method.getParameters());
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (JParameter jParameter : jParameters) {
            parameters.add(new Parameter(jParameter));
        }

        return parameters;
    }

    @Override
    protected void populateTemplateVariables(Map<String, Object> variables) {
        JClassType promiseType = promiseContext.getPromiseType();

        variables.put("resourceType", new ClassDefinition(resourceType).getParameterizedClassName());
        variables.put("resourcePromiseType", new ClassDefinition(promiseType).getParameterizedClassName());
        variables.put("methods", methodDefinitions);
        variables.put("imports", imports);
    }

    private void registerPromiseFactoryBinding() throws UnableToCompleteException {
        JGenericType resourceDelegateType = getType(ResourcePromiseFactory.class).isGenericType();
        JParameterizedType parameterizedResourceDelegateType = getContext().getTypeOracle().getParameterizedType(
                resourceDelegateType, new JClassType[]{promiseContext.getPromiseType()});
        ClassDefinition definition = new ClassDefinition(parameterizedResourceDelegateType);
        RegisterGinBindingEvent.postSingleton(eventBus, definition, getClassDefinition());
    }

}
