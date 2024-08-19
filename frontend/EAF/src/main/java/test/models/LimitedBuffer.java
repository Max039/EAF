package test.models;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class LimitedBuffer<T> {
    private final int maxSize;
    private final Queue<T> buffer;

    // Constructor to initialize the buffer with a given size
    public LimitedBuffer(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        this.maxSize = maxSize;
        this.buffer = new LinkedList<>();
    }

    // Method to add an element to the buffer
    public void add(T element) {
        if (!buffer.contains(element)) {
            if (buffer.size() == maxSize) {
                buffer.poll(); // Remove the oldest element
            }
            buffer.offer(element);
        }
        else {
            buffer.remove(element);
            buffer.add(element);
        }
    }

    // Method to retrieve and remove the oldest element from the buffer
    public T remove() {
        return buffer.poll();
    }

    // Method to check the current size of the buffer
    public int size() {
        return buffer.size();
    }

    // Method to check if the buffer is empty
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    // Method to get a view of the elements in the buffer
    public ArrayList<T> getElements() {
        ArrayList<T> list = new ArrayList<>(buffer);
        java.util.Collections.reverse(list); // Reverse the list to make the last added element at index 0
        return list;
    }

    // Method to retrieve the maximum size of the buffer
    public int getMaxSize() {
        return maxSize;
    }

    // Method to convert the buffer to a JSON array
    public JSONArray toJsonArray() {
        JSONArray jsonArray = new JSONArray();
        for (T element : buffer) {
            jsonArray.put(element);
        }
        return jsonArray;
    }

    // Method to create a LimitedBuffer instance from a JSON array
    public static <T> LimitedBuffer<T> fromJsonArray(Class<T> c, JSONArray jsonArray, int maxSize) {
        LimitedBuffer<T> buffer = new LimitedBuffer<>(maxSize);
        for (int i = 0; i < jsonArray.length(); i++) {
            @SuppressWarnings("unchecked")
            T element = (T) jsonArray.get(i);
            buffer.add(element);
        }
        return buffer;
    }

}
