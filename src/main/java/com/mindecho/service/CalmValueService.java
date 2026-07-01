package com.mindecho.service;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class CalmValueService {
    private static final CalmValueService INSTANCE = new CalmValueService();

    private final IntegerProperty calmValue = new SimpleIntegerProperty(0);

    private CalmValueService() {
    }

    public static CalmValueService getInstance() {
        return INSTANCE;
    }

    public IntegerProperty calmValueProperty() {
        return calmValue;
    }

    public int getCalmValue() {
        return calmValue.get();
    }

    public void increase(int delta) {
        calmValue.set(calmValue.get() + Math.max(0, delta));
    }
}
