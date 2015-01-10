package com.androideasyapps.phoenix.shared.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by seans on 10/01/15.
 */
public class PreferenceManager implements InvocationHandler {
    private final SharedPreferences preferences;

    public PreferenceManager(SharedPreferences preferences) {
        this.preferences=preferences;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(proxy, args);
        }

        if (args!=null&&args.length==1) {
            // set
            if (isBoolean(args[0].getClass())) {
                preferences.edit().putBoolean(method.getName(), (boolean)args[0]).apply();
            } else if (isFloat(args[0].getClass())) {
                preferences.edit().putFloat(method.getName(), (float)args[0]).apply();
            } else if (isInt(args[0].getClass())) {
                preferences.edit().putInt(method.getName(), (int) args[0]).apply();
            } else if (isLong(args[0].getClass())) {
                preferences.edit().putLong(method.getName(), (int) args[0]).apply();
            } else if (isString(args[0].getClass())) {
                preferences.edit().putString(method.getName(), (String) args[0]).apply();
            } else {
                throw new UnsupportedOperationException("PreferenceManager set preference with type of " + args.getClass() + " is not supported");
            }
            return null;
        } else {
            // get
            // Note: Android Preferences are stored as strings, so, we have to use getString() and then parse...
            // begs the question, why does Preference have getInteger() etc, if we can;t use them.
            if (isBoolean(method.getReturnType())) {
                return Boolean.parseBoolean(preferences.getString(method.getName(), "false"));
            } else if (isFloat(method.getReturnType())) {
                return Float.parseFloat(preferences.getString(method.getName(), "0"));
            } else if (isInt(method.getReturnType())) {
                return Integer.parseInt(preferences.getString(method.getName(), "0"));
            } else if (isLong(method.getReturnType())) {
                return Long.parseLong(preferences.getString(method.getName(), "0"));
            } else if (isString(method.getReturnType())) {
                return preferences.getString(method.getName(), "");
            } else {
                throw new UnsupportedOperationException("PreferenceManager get preference with return type of " + method.getReturnType() + " is not supported");
            }
        }
    }

    private boolean isString(Class<? extends Object> aClass) {
        return aClass == String.class;
    }

    private boolean isBoolean(Class<? extends Object> aClass) {
        return aClass==Boolean.class || aClass == boolean.class;
    }
    private boolean isFloat(Class<? extends Object> aClass) {
        return aClass==Float.class || aClass == float.class;
    }
    private boolean isInt(Class<? extends Object> aClass) {
        return aClass==Integer.class || aClass == int.class;
    }
    private boolean isLong(Class<? extends Object> aClass) {
        return aClass==Long.class || aClass == long.class;
    }

    /**
     * Given a Preferences class, create a new preferences object.
     *
     * @param klass
     * @param preferences
     * @param <T>
     * @return
     */
    public static <T> T getPreferences(Class<T> klass, SharedPreferences preferences) {
        return (T) Proxy.newProxyInstance(PreferenceManager.class.getClassLoader(), new Class[] {klass}, new PreferenceManager(preferences));
    }
}
