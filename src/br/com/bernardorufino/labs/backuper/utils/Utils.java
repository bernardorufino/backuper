package br.com.bernardorufino.labs.backuper.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.*;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
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

    public static File copy(File file, String location) throws IOException {
        return copy(file, new File(location));
    }

    public static File copy(File file, File location) throws IOException {
        return Files.copy(file.toPath(), location.toPath().resolve(file.getName()), REPLACE_EXISTING).toFile();
    }

    private static class Purger extends SimpleFileVisitor<Path> {

        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult postVisitDirectory(Path folder, IOException e) throws IOException {
            if (e == null) {
                Files.delete(folder);
                return FileVisitResult.CONTINUE;
            } else {
                throw e;
            }
        }
    }

    public static void purge(File folder) throws IOException {
        //TODO: Check if it walks through the folder container itself
        Files.walkFileTree(folder.toPath(), new Purger());
    }

    public static void createContentFile(File folder, String name, String content) throws IOException {
        FileUtils.writeStringToFile(folder.toPath().resolve(name).toFile(), content);
    }

    public static String stripExtension(String fileName) {
        return fileName.replaceFirst("\\.(.*?)$", "");
    }

    public static String readFromFile(File file, Charset charset) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return charset.decode(ByteBuffer.wrap(encoded)).toString();
    }

    public static String readFromFile(File file) throws IOException {
        return readFromFile(file, Charset.defaultCharset());
    }

    public static String joinLines(List<String> lines) {
        return StringUtils.join(lines, "\n");
    }

    public static List<String> readLines(File file) throws IOException {
        return Files.readAllLines(file.toPath(), Charset.defaultCharset());
    }

    public static File createFolder(File where, String name) throws IOException {
        return Files.createDirectory(where.toPath().resolve(name)).toFile();
    }

    public static File createFolder(String path) throws IOException {
        return Files.createDirectory(new File(path).toPath()).toFile();
    }

}



