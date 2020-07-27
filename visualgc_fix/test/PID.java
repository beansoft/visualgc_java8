import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class PID {
    public static void main(String[] args) throws Exception {
        int pid = getPid();
        System.out.println("pid: " + pid);
        System.in.read(); // block the program so that we can do some probing on it
    }
    
    private static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        System.out.println("name:" + name);
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }
}