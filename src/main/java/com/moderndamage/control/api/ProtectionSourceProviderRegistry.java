package com.moderndamage.control.api;

import java.util.ArrayList;
import java.util.List;

public class ProtectionSourceProviderRegistry {
    private static final List<IProtectionSourceProvider> PROVIDERS = new ArrayList<>();

    public static void register(IProtectionSourceProvider provider) {
        PROVIDERS.add(provider);
    }

    public static List<IProtectionSourceProvider> getProviders() {
        return PROVIDERS;
    }

    public static void clear() {
        PROVIDERS.clear();
    }
}