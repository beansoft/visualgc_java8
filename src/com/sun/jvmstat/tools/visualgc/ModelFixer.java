package com.sun.jvmstat.tools.visualgc;

import github.beansoftapp.visualgc.Exceptions;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredVm;

import java.lang.reflect.Field;

public class ModelFixer {
  public static boolean fixMetaspace(MonitoredVmModel testModel, MonitoredVm monitoredVm) {
    try {
      Field PERM_SIZE = MonitoredVmModel.class.getDeclaredField("permSize");
      Field PERM_CAPACITY = MonitoredVmModel.class.getDeclaredField("permCapacity");
      Field PERM_USED = MonitoredVmModel.class.getDeclaredField("permUsed");
      PERM_SIZE.setAccessible(true);
      PERM_CAPACITY.setAccessible(true);
      PERM_USED.setAccessible(true);
      if (PERM_SIZE.get(testModel) == null && PERM_CAPACITY
          .get(testModel) == null && PERM_USED
          .get(testModel) == null) {
        PERM_SIZE.set(testModel, monitoredVm.findByName("sun.gc.metaspace.maxCapacity"));
        PERM_CAPACITY.set(testModel, monitoredVm.findByName("sun.gc.metaspace.capacity"));
        PERM_USED.set(testModel, monitoredVm.findByName("sun.gc.metaspace.used"));
        return true;
        // this.hasMetaspace = true;
      }
    } catch (NoSuchFieldException ex) {
      Exceptions.printStackTrace(ex);
    } catch (SecurityException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IllegalArgumentException ex) {
      Exceptions.printStackTrace(ex);
    } catch (IllegalAccessException ex) {
      Exceptions.printStackTrace(ex);
    } catch (MonitorException ex) {
      Exceptions.printStackTrace((Throwable)ex);
    }

    return false;
  }
}
