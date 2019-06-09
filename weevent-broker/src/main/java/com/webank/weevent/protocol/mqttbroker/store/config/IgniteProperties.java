package com.webank.weevent.protocol.mqttbroker.store.config;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
public class IgniteProperties {
    /**
     * Persistent cache min sise(MB), default: 64
     */
    private int persistenceInitialSize = 64;

    /**
     * Persistent cache max sise(MB), default: 128
     */
    private int persistenceMaxSize = 128;

    /**
     * store path
     */
    private String persistenceStorePath;

    /**
     * non-Persistent cache min sise(MB), default: 64
     */
    private int NotPersistenceInitialSize = 64;

    /**
     * non-Persistent cache max sise(MB), default: 128
     */
    private int NotPersistenceMaxSize = 128;

    public int getPersistenceInitialSize() {
        return persistenceInitialSize;
    }

    public IgniteProperties setPersistenceInitialSize(int persistenceInitialSize) {
        this.persistenceInitialSize = persistenceInitialSize;
        return this;
    }

    public int getPersistenceMaxSize() {
        return persistenceMaxSize;
    }

    public IgniteProperties setPersistenceMaxSize(int persistenceMaxSize) {
        this.persistenceMaxSize = persistenceMaxSize;
        return this;
    }

    public String getPersistenceStorePath() {
        return persistenceStorePath;
    }

    public IgniteProperties setPersistenceStorePath(String persistenceStorePath) {
        this.persistenceStorePath = persistenceStorePath;
        return this;
    }

    public int getNotPersistenceInitialSize() {
        return NotPersistenceInitialSize;
    }

    public IgniteProperties setNotPersistenceInitialSize(int notPersistenceInitialSize) {
        NotPersistenceInitialSize = notPersistenceInitialSize;
        return this;
    }

    public int getNotPersistenceMaxSize() {
        return NotPersistenceMaxSize;
    }

    public IgniteProperties setNotPersistenceMaxSize(int notPersistenceMaxSize) {
        NotPersistenceMaxSize = notPersistenceMaxSize;
        return this;
    }
}
