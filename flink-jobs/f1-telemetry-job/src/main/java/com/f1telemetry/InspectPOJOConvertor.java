package com.f1telemetry;

import java.lang.reflect.*;

/**
 * Chạy tool này TRƯỚC khi build lại ClickHouseSinkFactory đã sửa, để xác nhận
 * chính xác các method writeXxx() thực sự tồn tại trên DataWriter, và chữ ký
 * (kiểu tham số) của chúng — tránh trường hợp code không compile vì đoán sai
 * tên/overload (writeUInt8/writeUInt16/writeUInt32 là suy đoán theo pattern,
 * cần xác nhận lại với đúng version connector bạn đang dùng).
 *
 * Cách chạy:
 *   javac -cp <flink-connector-clickhouse.jar>:. InspectPOJOConvertor.java
 *   java  -cp <flink-connector-clickhouse.jar>:. com.f1telemetry.InspectPOJOConvertor
 */
public class InspectPOJOConvertor {
    public static void main(String[] args) {
        inspect("org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink");
        inspect("org.apache.flink.connector.clickhouse.convertor.DataWriter");
        inspect("org.apache.flink.connector.clickhouse.convertor.POJOConvertor");
    }

    private static void inspect(String className) {
        System.out.println("\n================ " + className + " ================");
        try {
            Class<?> clazz = Class.forName(className);
            System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));

            System.out.println("\nConstructors:");
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                System.out.println("  " + c.toString());
            }

            System.out.println("\nMethods (chú ý các method bắt đầu bằng 'write'):");
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().startsWith("write") || Modifier.isPublic(m.getModifiers())) {
                    System.out.println("  " + m.toGenericString());
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("KHÔNG TÌM THẤY class này trên classpath: " + className);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}