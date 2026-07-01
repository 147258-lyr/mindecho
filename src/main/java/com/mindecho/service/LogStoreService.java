package com.mindecho.service;

import com.mindecho.model.DestructionLog;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface LogStoreService {
    void save(DestructionLog log) throws Exception;
    List<DestructionLog> findByDate(LocalDate date);
    List<DestructionLog> findByMonth(YearMonth month);
    Optional<DestructionLog> findRandom();
    void deleteAll() throws Exception;
    int countAll();
}
