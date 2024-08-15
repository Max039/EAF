package test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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
            o = DragDropRectanglesWithSplitPane.readJSONFileToJSON(cachePath + "." + cacheFileType);
            System.out.println("Cache found!");
        }
        catch (Exception e) {
            System.out.println("Cache could not be found!");
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
            buffer.put(b.getKey(), makeBuffer(b.getValue().getFirst(), b.getValue().getSecond(), (JSONArray) o.get(b.getKey())));
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
        DragDropRectanglesWithSplitPane.writeJSONToFile(toCache(), cachePath + "." + cacheFileType);
    }

    public <T> void addToBuffer(String b, T item) {
        LimitedBuffer<T> buf = (LimitedBuffer<T>) buffer.get(b);
        buf.add(item);
        System.out.println("=====================");
        System.out.println("Cache \"" + b + "\" updated to: ");
        print(b);
        System.out.println("=====================");
        saveCache();
    }

    public void print(String buf) {
        for (var e : buffer.get(buf).getElements()) {
            System.out.println(e.toString());
        }
    }

}
