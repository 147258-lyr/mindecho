package com.mindecho;

import com.mindecho.service.*;
import com.mindecho.service.impl.*;
import com.mindecho.util.*;

public class ServiceLocator {
    private static ServiceLocator instance;
    
    private final ConfigManager configManager;
    private final Encryptor encryptor;
    private final EmotionEventBus emotionEventBus;
    private final FallbackPhraseStore fallbackPhraseStore;
    private final AiEngineService aiEngineService;
    private final LogStoreService logStoreService;
    private final ScratchQuotaService scratchQuotaService;
    private final WeatherEngine weatherEngine;
    private final ResonanceAudioEngine resonanceAudioEngine;
    private final ReportEngine reportEngine;
    private final EmotionCapsuleService emotionCapsuleService;
    private final EmotionArticleService emotionArticleService;
    
    private ServiceLocator() {
        configManager = new ConfigManager();
        encryptor = new Encryptor();
        emotionEventBus = EmotionEventBus.getInstance();
        fallbackPhraseStore = FallbackPhraseStore.getInstance();
        aiEngineService = new OpenAiEngine(configManager);
        logStoreService = new SqliteLogStore(encryptor, emotionEventBus);
        scratchQuotaService = new SqliteScratchQuotaStore();
        weatherEngine = new WeatherEngine();
        resonanceAudioEngine = new ResonanceAudioEngine(configManager.isAudioEnabled());
        reportEngine = new ReportEngine(logStoreService, aiEngineService);
        emotionCapsuleService = new SqliteCapsuleStore();
        emotionArticleService = new DefaultArticleService(aiEngineService);
    }
    
    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }
    
    public ConfigManager getConfigManager() { return configManager; }
    public Encryptor getEncryptor() { return encryptor; }
    public EmotionEventBus getEmotionEventBus() { return emotionEventBus; }
    public FallbackPhraseStore getFallbackPhraseStore() { return fallbackPhraseStore; }
    public AiEngineService getAiEngineService() { return aiEngineService; }
    public LogStoreService getLogStoreService() { return logStoreService; }
    public ScratchQuotaService getScratchQuotaService() { return scratchQuotaService; }
    public WeatherEngine getWeatherEngine() { return weatherEngine; }
    public ResonanceAudioEngine getResonanceAudioEngine() { return resonanceAudioEngine; }
    public ReportEngine getReportEngine() { return reportEngine; }
    public EmotionCapsuleService getEmotionCapsuleService() { return emotionCapsuleService; }
    public EmotionArticleService getEmotionArticleService() { return emotionArticleService; }
}
