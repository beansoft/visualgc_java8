package com.sun.jvmstat.monitor;

import sun.jvmstat.monitor.StringMonitor;

public class HolderStringMonitor extends AbstractHolderMonitor implements StringMonitor {
    private String value;

    public HolderStringMonitor(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public String stringValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     * The object returned contains a String with a copy of the current
     * value of the StringInstrument.
     *
     * @return Object - a copy of the current value of the StringInstrument.
     *                  The return value is guaranteed to be of type String.
     */
    public Object getValue() {
        return stringValue();
    }
}
