package com.f1telemetry;

import java.lang.reflect.*;

public class InspectPOJOConvertor {
    public static void main(String[] args) {
        inspectClass("com.f1telemetry.avro.CarTelemetryEvent");
        inspectClass("com.f1telemetry.model.TelemetryRollup");
        inspectClass("org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink");
        inspectClass("org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSinkBuilder");
        inspectClass("org.apache.flink.connector.clickhouse.sink.ClickHouseClientConfig");
        
        // Let's try to load common convertor classes to see which ones exist.
        String[] possibleConvertors = {
            "org.apache.flink.connector.clickhouse.convertor.POJOConvertor",
            "org.apache.flink.connector.clickhouse.convertor.ClickHouseConvertor",
            "org.apache.flink.connector.clickhouse.convertor.ClickHouseRowConverter",
            "org.apache.flink.connector.clickhouse.internal.convertor.ClickHouseRowConverter"
        };
        for (String c : possibleConvertors) {
            try {
                Class<?> clazz = Class.forName(c);
                System.out.println("\nFound Convertor Class: " + clazz.getName());
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    System.out.println("  Constructor: " + constructor);
                }
                for (Method m : clazz.getDeclaredMethods()) {
                    System.out.println("  Method: " + m);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Not found: " + c);
            }
        }
    }

    private static void inspectClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            System.out.println("\n========================================");
            System.out.println("Class: " + clazz.getName());
            System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));
            
            System.out.println("\nConstructors:");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                System.out.println("  " + c.toString());
            }
            
            System.out.println("\nFields:");
            for (Field f : clazz.getDeclaredFields()) {
                System.out.println("  " + f.toString());
            }
            
            System.out.println("\nMethods:");
            for (Method m : clazz.getMethods()) {
                System.out.println("  " + m.toString());
            }
        } catch (Exception e) {
            System.err.println("Failed to inspect " + className + ": " + e.getMessage());
        }
    }
}
