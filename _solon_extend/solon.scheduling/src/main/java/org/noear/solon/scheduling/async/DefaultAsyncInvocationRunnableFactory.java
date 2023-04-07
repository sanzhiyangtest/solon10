package org.noear.solon.scheduling.async;

import org.noear.solon.core.aspect.Invocation;

/**
 * @author orangej
 * @since 2023/4/7
 */
public class DefaultAsyncInvocationRunnableFactory implements IAsyncInvocationRunnableFactory{
    @Override
    public Runnable create(Invocation inv) {
        return new AsyncInvocationRunnable(inv);
    }
}
