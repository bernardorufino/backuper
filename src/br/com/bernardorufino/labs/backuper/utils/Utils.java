package br.com.bernardorufino.labs.backuper.utils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.FileTime;
import java.util.Map;

public class Utils {


    public static class FileDate {
        public DateTime createdAt, modifiedAt;
    }

    public static FileDate getFileDate(File file) throws IOException {
        FileDate date = new FileDate();
        Map<String, Object> attrs = Files.readAttributes(file.toPath(), "creationTime,lastModifiedTime");
        FileTime createdAt = (FileTime) attrs.get("creationTime");
        date.createdAt = new DateTime(createdAt.toMillis());
        FileTime modifiedAt = (FileTime) attrs.get("lastModifiedTime");
        date.modifiedAt = new DateTime(modifiedAt.toMillis());
        return date;
    }

    public static DateTime getDate(File file) throws IOException {
        FileDate date = getFileDate(file);
        return date.createdAt.isAfter(date.modifiedAt) ? date.createdAt : date.modifiedAt;
    }

    public static void copy(File file, String location) throws IOException {
        Files.copy(file.toPath(), new File(location).toPath(), REPLACE_EXISTING);
    }

}
