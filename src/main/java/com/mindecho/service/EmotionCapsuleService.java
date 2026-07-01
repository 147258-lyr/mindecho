package com.mindecho.service;

import com.mindecho.model.EmotionCapsule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmotionCapsuleService {
    void save(EmotionCapsule capsule) throws Exception;
    void update(EmotionCapsule capsule) throws Exception;
    Optional<EmotionCapsule> findById(Long id);
    List<EmotionCapsule> findAll();
    List<EmotionCapsule> findByDate(LocalDate date);
    List<EmotionCapsule> findUnlocked();
    List<EmotionCapsule> findLocked();
    int countAll();
}