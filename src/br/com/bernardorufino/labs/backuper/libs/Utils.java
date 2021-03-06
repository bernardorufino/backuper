package br.com.bernardorufino.labs.backuper.libs;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import com.sun.nio.file.ExtendedCopyOption;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Utils {

    public static <K, V> Map<K, V> extract(Map<K, V> map, Collection<K> keys) {
        Map<K, V> newMap = new HashMap<>();
        for (K key : keys) {
            newMap.put(key, map.get(key));
        }
        return newMap;
    }

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

    public static File getFsNode(File folder, String name) throws IOException {
        return folder.toPath().resolve(name).toFile();
    }

    public static File copyIntoFolder(File file, String location) throws IOException {
        return copyIntoFolder(file, new File(location));
    }

    public static File copyIntoFolder(File file, File location) throws IOException {
        return copy(file, location.toPath().resolve(file.getName()).toString());
    }

    public static boolean interruptable = true;

    public static void uninterruptable() { interruptable = false; }
    public static void interruptable() { interruptable = true; }

    public static File copy(File file, String location) throws IOException {
        File target = new File(location);
        target.getParentFile().mkdirs();
        // COPY_ATTRIBUTES ?
        if (interruptable) {
            return Files.copy(file.toPath(), target.toPath(), REPLACE_EXISTING, ExtendedCopyOption.INTERRUPTIBLE).toFile();
        } else {
            return Files.copy(file.toPath(), target.toPath(), REPLACE_EXISTING).toFile();
        }
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
        if (folder.exists()) {
            Files.walkFileTree(folder.toPath(), new Purger());
        }
    }

    public static void delete(File file) throws IOException {
        Files.delete(file.toPath());
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
        File folder = new File(path);
        folder.mkdirs();
        return folder;
    }

    public static String joinPaths(String... paths) {
        StringBuilder joint = new StringBuilder();
        for (String path : paths) {
            joint.append(Node.SEPARATOR);
            joint.append(StringUtils.strip(path, Node.SEPARATOR));
        }
        return StringUtils.strip(joint.toString(), Node.SEPARATOR);
    }

    public static boolean isAfter(DateTime a, DateTime b) {
        return Definitions.dateComparator.compare(a, b) > 0;
    }

}



