package com.vl.salesman.bundlewrapper;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public abstract class BaseBundleWrapper {
    private final Bundle bundle;
    private final Gson gson = new Gson();

    public BaseBundleWrapper(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void merge(Bundle bundle) {
        this.bundle.putAll(bundle);
    }

    protected <T> T get(String extra, Class<T> token) {
        return gson.fromJson(bundle.getString(extra), TypeToken.get(token));
    }

    protected <T> T get(String extra, TypeToken<T> token) {
        return gson.fromJson(bundle.getString(extra), token);
    }

    protected <T> void put(String extra, T data) {
        bundle.putString(extra, gson.toJson(data));
    }
}
