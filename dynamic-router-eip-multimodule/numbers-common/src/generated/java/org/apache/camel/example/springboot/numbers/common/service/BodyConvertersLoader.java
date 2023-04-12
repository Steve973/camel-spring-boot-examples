/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.example.springboot.numbers.common.service;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.DeferredContextBinding;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.TypeConverterLoaderException;
import org.apache.camel.spi.TypeConverterLoader;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.camel.support.SimpleTypeConverter;
import org.apache.camel.support.TypeConverterSupport;
import org.apache.camel.util.DoubleMap;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@SuppressWarnings("unchecked")
@DeferredContextBinding
public final class BodyConvertersLoader implements TypeConverterLoader, CamelContextAware {

    private CamelContext camelContext;

    public BodyConvertersLoader() {
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void load(TypeConverterRegistry registry) throws TypeConverterLoaderException {
        registerConverters(registry);
    }

    private void registerConverters(TypeConverterRegistry registry) {
        addTypeConverter(registry, org.apache.camel.example.springboot.numbers.common.model.ControlMessage.class, byte[].class, false,
            (type, exchange, value) -> getBodyConverters().bytesToControlMessage((byte[]) value));
    }

    private static void addTypeConverter(TypeConverterRegistry registry, Class<?> toType, Class<?> fromType, boolean allowNull, SimpleTypeConverter.ConversionMethod method) { 
        registry.addTypeConverter(toType, fromType, new SimpleTypeConverter(allowNull, method));
    }

    private volatile org.apache.camel.example.springboot.numbers.common.service.BodyConverters bodyConverters;
    private org.apache.camel.example.springboot.numbers.common.service.BodyConverters getBodyConverters() {
        if (bodyConverters == null) {
            bodyConverters = new org.apache.camel.example.springboot.numbers.common.service.BodyConverters();
            CamelContextAware.trySetCamelContext(bodyConverters, camelContext);
        }
        return bodyConverters;
    }
}
