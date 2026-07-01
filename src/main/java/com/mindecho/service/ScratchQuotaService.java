package com.mindecho.service;

public interface ScratchQuotaService {
    int getRemainingToday();
    void consumeOne();
    boolean isExhausted();
}
