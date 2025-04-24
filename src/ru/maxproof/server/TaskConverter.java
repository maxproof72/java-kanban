package ru.maxproof.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskConverter {

    static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime != null) {
                jsonWriter.value(localDateTime.format(timeFormatter));
            } else {
                jsonWriter.nullValue();
            }
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), timeFormatter);
        }
    }

    static class DurationTypeAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            if (duration != null) {
                jsonWriter.value(duration.toMinutes());
            } else {
                jsonWriter.nullValue();
            }
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            return Duration.ofMinutes(Long.parseLong(jsonReader.nextString()));
        }
    }

    private static Gson createTaskJsonConverter() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return createTaskJsonConverter().fromJson(json, clazz);
    }

    public static String toJson(Object obj) {
        return createTaskJsonConverter().toJson(obj);
    }

    static <T> List<T> listFromJson(String json, Class<T> clazz) {
        var typeToken = TypeToken.getParameterized(List.class, clazz);
        return createTaskJsonConverter().fromJson(json, typeToken.getType());
    }
}
