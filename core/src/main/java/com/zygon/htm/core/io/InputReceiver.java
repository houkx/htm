package com.zygon.htm.core.io;

/**
 *
 * @author davec
 * @param <T>
 */
public interface InputReceiver<T extends Input<?>> {
    public String getId();
    public void send (T input);
}
