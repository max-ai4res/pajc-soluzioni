package it.unibs.pajc.redolfi.e08;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Esercizio 6.8: una cache con lettori e scrittori, in tre varianti selezionate dal
 * costruttore: "rw" (HashMap + ReentrantReadWriteLock), "monitor" (HashMap +
 * synchronized), "concurrent" (ConcurrentHashMap, nessun lock esplicito).
 */
public class Cache<K, V> {
    private final String modo;
    private final Map<K, V> mappa;
    private final ReadWriteLock rw = new ReentrantReadWriteLock();

    public Cache(String modo) {
        this.modo = modo;
        this.mappa = modo.equals("concurrent") ? new ConcurrentHashMap<>() : new HashMap<>();
    }

    public V get(K k) {
        switch (modo) {
            case "rw" -> {
                rw.readLock().lock();
                try { return mappa.get(k); } finally { rw.readLock().unlock(); }
            }
            case "monitor" -> { synchronized (this) { return mappa.get(k); } }
            default -> { return mappa.get(k); }
        }
    }

    public void put(K k, V v) {
        switch (modo) {
            case "rw" -> {
                rw.writeLock().lock();
                try { mappa.put(k, v); } finally { rw.writeLock().unlock(); }
            }
            case "monitor" -> { synchronized (this) { mappa.put(k, v); } }
            default -> mappa.put(k, v);
        }
    }
}
