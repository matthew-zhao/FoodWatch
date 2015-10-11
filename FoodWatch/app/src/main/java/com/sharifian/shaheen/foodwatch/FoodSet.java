package com.sharifian.shaheen.foodwatch;

import java.util.HashSet;

/**
 * Created by MattZhao on 10/10/15.
 */
public class FoodSet<E> extends java.lang.Object {
    private HashSet<E> foodSet;

    public FoodSet() {
        foodSet = new HashSet<E>();
    }

    public boolean add(E e) {
        return foodSet.add(e);
    }

    public void clear() {
        foodSet.clear();
    }

    public boolean contains(Object o) {
        return foodSet.contains(o);
    }

    public boolean remove(Object o) {
        return foodSet.remove(o);
    }

    public int size() {
        return foodSet.size();
    }

    public boolean isEmpty() {
        return foodSet.isEmpty();
    }
}
