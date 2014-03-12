package com.blogspot.mydailyjava.bytebuddy.instrumentation;

import com.blogspot.mydailyjava.bytebuddy.dynamic.DynamicType;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.method.MethodDescription;
import com.blogspot.mydailyjava.bytebuddy.instrumentation.type.auxiliary.AuxiliaryType;
import com.blogspot.mydailyjava.bytebuddy.utility.MockitoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class InstrumentationContextDefaultTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private Instrumentation.Context.Default.AuxiliaryTypeNamingStrategy auxiliaryTypeNamingStrategy;
    @Mock
    private AuxiliaryType.MethodAccessorFactory methodAccessorFactory;
    @Mock
    private AuxiliaryType firstAuxiliary, secondAuxiliary;
    @Mock
    private DynamicType<?> firstDynamic, secondDynamic;
    @Mock
    private MethodDescription firstMethod, firstProxyMethod, secondMethod, secondProxyMethod;

    private Instrumentation.Context.Default defaultContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        defaultContext = new Instrumentation.Context.Default(auxiliaryTypeNamingStrategy, methodAccessorFactory);
        when(firstAuxiliary.make(any(String.class), any(AuxiliaryType.MethodAccessorFactory.class))).thenReturn((DynamicType) firstDynamic);
        when(secondAuxiliary.make(any(String.class), any(AuxiliaryType.MethodAccessorFactory.class))).thenReturn((DynamicType) secondDynamic);
        when(firstDynamic.getName()).thenReturn(FOO);
        when(secondDynamic.getName()).thenReturn(BAR);
        when(auxiliaryTypeNamingStrategy.name(any(AuxiliaryType.class))).thenReturn(QUX, BAZ);
        when(methodAccessorFactory.requireAccessorMethodFor(firstMethod)).thenReturn(firstProxyMethod);
        when(methodAccessorFactory.requireAccessorMethodFor(secondMethod)).thenReturn(secondProxyMethod);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleRegistration() throws Exception {
        assertThat(defaultContext.register(firstAuxiliary), is(FOO));
        assertThat(defaultContext.register(secondAuxiliary), is(BAR));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes().size(), is(2));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes(), hasItems(firstDynamic, secondDynamic));
        verify(firstAuxiliary).make(QUX, defaultContext);
        verify(secondAuxiliary).make(BAZ, defaultContext);
        verifyNoMoreInteractions(firstAuxiliary);
        verifyNoMoreInteractions(secondAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(firstAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(secondAuxiliary);
        verifyNoMoreInteractions(auxiliaryTypeNamingStrategy);
        verifyZeroInteractions(methodAccessorFactory);
    }

    @Test
    public void testDoubleRegistration() throws Exception {
        assertThat(defaultContext.register(firstAuxiliary), is(FOO));
        assertThat(defaultContext.register(firstAuxiliary), is(FOO));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes().size(), is(1));
        assertThat(defaultContext.getRegisteredAuxiliaryTypes(), hasItem(firstDynamic));
        verify(firstAuxiliary).make(QUX, defaultContext);
        verifyNoMoreInteractions(firstAuxiliary);
        verify(auxiliaryTypeNamingStrategy).name(firstAuxiliary);
        verifyNoMoreInteractions(auxiliaryTypeNamingStrategy);
        verifyZeroInteractions(methodAccessorFactory);
    }

    @Test
    public void testMethodProxyFactory() throws Exception {
        assertThat(defaultContext.requireAccessorMethodFor(firstMethod), is(firstProxyMethod));
        assertThat(defaultContext.requireAccessorMethodFor(secondMethod), is(secondProxyMethod));
        assertThat(defaultContext.requireAccessorMethodFor(firstMethod), is(firstProxyMethod));
        verify(methodAccessorFactory).requireAccessorMethodFor(firstMethod);
        verify(methodAccessorFactory).requireAccessorMethodFor(secondMethod);
        verifyNoMoreInteractions(methodAccessorFactory);
        verifyZeroInteractions(auxiliaryTypeNamingStrategy);
    }
}
