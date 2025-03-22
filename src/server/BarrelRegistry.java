package server;

import java.util.ArrayList;
import java.util.List;

public class BarrelRegistry {
    private static List<String> barrelAddresses = new ArrayList<>();

    static {
        // Endereços RMI dos barrels disponíveis
        barrelAddresses.add("rmi://localhost/Barrel1");
        barrelAddresses.add("rmi://localhost/Barrel2");
    }

    public static List<String> getBarrelAddresses() {
        return barrelAddresses;
    }
}
