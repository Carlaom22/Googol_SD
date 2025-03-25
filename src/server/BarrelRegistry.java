package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BarrelRegistry {
    private static final List<String> barrelAddresses = new ArrayList<>();

    static {
        // Endereços iniciais
        registerBarrel("rmi://localhost/Barrel1");
        registerBarrel("rmi://localhost/Barrel2");
    }

    public static synchronized void registerBarrel(String address) {
        if (!barrelAddresses.contains(address)) {
            barrelAddresses.add(address);
            System.out.println("[BarrelRegistry] Registered: " + address);
        }
    }

    public static synchronized void removeBarrel(String address) {
        if (barrelAddresses.remove(address)) {
            System.out.println("[BarrelRegistry] Removed: " + address);
        }
    }

    public static synchronized List<String> getBarrelAddresses() {
        return Collections.unmodifiableList(new ArrayList<>(barrelAddresses));
    }

    public static synchronized boolean isEmpty() {
        return barrelAddresses.isEmpty();
    }
}
