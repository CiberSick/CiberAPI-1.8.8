package com.ciber.api.object;

import java.lang.reflect.Field;

public class CiberMessage {

    public static void reload(Class<?> clazz, CiberConfig config) {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                if (!config.contains(field.getName())) {
                    config.setString("prefix", "&6[Ciber Messages]");
                    config.set(field.getName(), field.get(null).toString().replace("§", "&"));
                    config.save();
                }

                String prefix = config.getString("prefix");
                field.set(0, config.getString(field.getName())
                        .replace("$prefix", prefix).replace("&", "§"));

            } catch (Exception ignored) {
            }
        }
        config.save();
    }
}
