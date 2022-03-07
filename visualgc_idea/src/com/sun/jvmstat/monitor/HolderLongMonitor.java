package com.sun.jvmstat.monitor;

import sun.jvmstat.monitor.LongMonitor;
import sun.jvmstat.monitor.StringMonitor;

public class HolderLongMonitor extends AbstractHolderMonitor implements LongMonitor {
    private long value;

    public HolderLongMonitor(String name, long value) {
        super(name);
        this.value = value;
    }

    /**
     * {@inheritDoc}
     * The object returned contains a Long object containing the
     * current value of the LongInstrument.
     *
     * @return Object - the current value of the the LongInstrument. The
     *                  return type is guaranteed to be of type Long.
     */
    public Object getValue() {
        return longValue();
    }

    /**
     * Return the current value of the LongInstrument as an long.
     *
     * @return long - the current value of the LongInstrument
     */
    @Override
    public long longValue() {
        return value;
    }
}
