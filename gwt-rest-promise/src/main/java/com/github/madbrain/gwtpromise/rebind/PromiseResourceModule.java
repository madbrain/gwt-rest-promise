package com.github.madbrain.gwtpromise.rebind;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gwtplatform.dispatch.rest.rebind.extension.ExtensionGenerator;

public class PromiseResourceModule extends AbstractModule {
    @Override
    protected void configure() {
         Multibinder.newSetBinder(binder(), ExtensionGenerator.class).addBinding().to(PromiseResourceExtensionGenerator.class);
    }
}
