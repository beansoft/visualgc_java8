package com.sun.jvmstat.monitor;

import sun.jvmstat.monitor.Monitor;
import sun.jvmstat.monitor.Units;
import sun.jvmstat.monitor.Variability;

public class AbstractHolderMonitor implements Monitor {
    protected String name;

    public AbstractHolderMonitor(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this instrumentation object.
     *
     * @return String - the name assigned to this instrumentation monitoring
     *                  object
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the base name of this instrumentation object.
     * The base name is the component of the name following the last
     * "." character in the name.
     *
     * @return String - the base name of the name assigned to this
     *                  instrumentation monitoring object.
     */
    @Override
    public String getBaseName() {
        int baseIndex = name.lastIndexOf(".")+1;
        return name.substring(baseIndex);
    }

    @Override
    public Units getUnits() {
        return null;
    }

    @Override
    public Variability getVariability() {
        return null;
    }

    @Override
    public boolean isVector() {
        return false;
    }

    @Override
    public int getVectorLength() {
        return 0;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public Object getValue() {
        return null;
    }
}
