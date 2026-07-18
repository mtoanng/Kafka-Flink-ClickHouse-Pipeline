package com.f1telemetry;

import java.lang.reflect.*;

public class InspectPOJOConvertor {
    public static void main(String[] args) {
        try {
            Class<?> clazz = Class.forName("org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink");
            System.out.println("Class: " + clazz.getName());
            System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));
            
            System.out.println("\nConstructors:");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                System.out.println("  " + c.toString());
            }
            
            System.out.println("\nMethods:");
            for (Method m : clazz.getDeclaredMethods()) {
                System.out.println("  " + m.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
