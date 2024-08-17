package test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class CacheManager {

    boolean foundCache;

    private HashMap<String, Pair<Class<?>, Integer>> bufferNamesAndSize = new HashMap<>();

    private HashMap<String, LimitedBuffer<?>> buffer = new HashMap<>();

    private String cachePath = "session";

    private String cacheFileType = "cache";


    CacheManager() {

        bufferNamesAndSize.put("filesOpened", new Pair<>(String.class, 5));

        JSONObject o = null;
        try {
            o = FileManager.readJSONFileToJSON(cachePath + "." + cacheFileType);
            System.out.println(LogManager.cacheManager() + " Cache found!");
        }
        catch (Exception e) {
            System.out.println(LogManager.cacheManager() + " Cache could not be found!");
        }
        if (o != null) {
            readCache(o);
            foundCache = true;
        }
        else {
            withoutCache();
            foundCache = false;
        }
    }

    public void readCache(JSONObject o) {
        for (var b : bufferNamesAndSize.entrySet()) {
            if (o.has(b.getKey())) {
                buffer.put(b.getKey(), makeBuffer(b.getValue().getFirst(), b.getValue().getSecond(), (JSONArray) o.get(b.getKey())));
            }
            else {
                System.out.println(LogManager.cacheManager() + LogManager.error() + " Field \"" + b.getKey() + "\" was not found in cache!");
                buffer.put(b.getKey(), makeBuffer(b.getValue().getFirst(), b.getValue().getSecond()));
            }
        }
    }

    public void withoutCache() {
        for (var b : bufferNamesAndSize.entrySet()) {
            buffer.put(b.getKey(), makeBuffer(b.getValue().getFirst(), b.getValue().getSecond()));
        }
    }

    public <T> LimitedBuffer<T> makeBuffer(Class<T> c, int size) {
        return new LimitedBuffer<T>(size);
    }

    public <T> LimitedBuffer<T> makeBuffer(Class<T> c, int size, JSONArray a) {
        return LimitedBuffer.fromJsonArray(c, a, size);
    }

    private JSONObject toCache() {
        JSONObject o = new JSONObject();
        for (var b : buffer.entrySet()) {
            o.put(b.getKey(), b.getValue().toJsonArray());
        }
        return o;
    }

    public void saveCache() {
        FileManager.writeJSONToFile(toCache(), cachePath + "." + cacheFileType);
    }

    public <T> void addToBuffer(String b, T item) {
        LimitedBuffer<T> buf = (LimitedBuffer<T>) buffer.get(b);
        buf.add(item);
        System.out.println(LogManager.cacheManager() + " Cache \"" + b + "\" updated to: ");
        print(b);
        saveCache();
    }

    public void print(String buf) {
        for (var e : buffer.get(buf).getElements()) {
            System.out.println(LogManager.cacheManager() + " " + e.toString());
        }
    }

    public <T> LimitedBuffer<T> getBuffer(Class<T> c, String s) {
        return (LimitedBuffer<T>) buffer.get(s);
    }

    public <T> T getFirstElement(Class<T> cl, String str) {
        var c = getBuffer(cl, str);
        if (!c.isEmpty()) {
            return c.getElements().get(0);
        }
        else {
            return null;
        }
    }

}
