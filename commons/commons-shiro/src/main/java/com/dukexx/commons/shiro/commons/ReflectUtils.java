package com.dukexx.commons.shiro.commons;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author dukexx
 * @date 2017/4/14
 * @since 1.0.0
 */
public class ReflectUtils {

    public static Object readDeclareField(Object target,String name) {
        if (target == null || name == null) {
            throw new IllegalArgumentException("argument target or name cannot be null");
        }
        Class clazz = target.getClass();
        Object value = null;
        while (true) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                value = field.get(target);
                field.setAccessible(false);
                return value;
            } catch (NoSuchFieldException e) {
                //本类中不存在
                try {
                    Type type = clazz.getGenericSuperclass();
                    clazz = getRawClassFromType(type);
                } catch (TypeNotPresentException e1) {
                    //父类不存在
                    break;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static Class getRawClassFromType(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        }
        throw new RuntimeException("cannot resolver class");
    }

}
