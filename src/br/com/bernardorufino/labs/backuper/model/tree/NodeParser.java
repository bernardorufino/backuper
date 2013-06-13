package br.com.bernardorufino.labs.backuper.model.tree;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeParser {

    public static final String INDENTATION = "  ";
    public static final String DELIMITER = " ";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.mediumDateTime();

    public static void main(String[] args) {

    }

    private static final String DELIMITER_REPLACEMENT = "_";

    /* package private */ static String toProperty(String string) {
        if (string.contains("_")) throw new RuntimeException("Change delimiter replacement in NodeParser.REPLACEMENT");
        return "<" + string.toUpperCase().replace(DELIMITER, DELIMITER_REPLACEMENT) + ">";
    }

    /* package private */ static String fromProperty(String property) {
        return StringUtils.capitalize(StringUtils.strip(property, "<>").toLowerCase().replace(DELIMITER_REPLACEMENT, DELIMITER));
    }

    public static Node fromList(String list, File location) {
        String[] lines = list.split("\n");
        Stack<FolderNode> folders = new Stack<>();
        FolderNode root = FolderNode.getDummy(), folder = root;
        folders.push(root);
        int level = 0;
        for (String line : lines) {
            LineData data = parseLine(line, location);
            if (data == null || data.node == null) continue;
            // If current level is higher than last, then push the last folder to the stack
            // Else if the current level is lower than the last, pop folders from stack the
            // number of times equals to the difference between those levels.
            if (data.level > level) {
                folders.push(folder);
            } else if (data.level < level) {
                for (int i = data.level; i < level; i += 1)
                    folders.pop();
            }
            folders.peek().addChild(data.node);
            level = data.level;
            if (data.node.isFolder()) folder = (FolderNode) data.node;
        }
        return root.getChildren().get(0);
    }

    private static class LineData {
        Node node;
        int level;

        public String toString() {
            return "(level = " + level +"; node = " + node + ")";
        }

    }

    public static LineData parseLine(String line, File location) {
        // Parse initial indentation (usually whitespaces) counting currentLevel and
        // use the remaining string to create a node.
        LineData result = new LineData();
        Matcher m = Pattern.compile("^((?:" + INDENTATION + ")*)(.+)$").matcher(line);
        m.lookingAt();
        // Set the depth level by the indentation
        result.level = m.group(1).length() / INDENTATION.length();
        Scanner scan = new Scanner(m.group(2)).useDelimiter(DELIMITER);
        // <TYPE> <STATUS> <DATE> NameOfTheFileOrFolder
        Node.Type type = Node.Type.valueOf(fromProperty(scan.next()));
        Node.Status status = Node.Status.valueOf(fromProperty(scan.next()));
        DateTime date = DATE_FORMATTER.parseDateTime(fromProperty(scan.next()));
        String name = scan.next();
        result.node = Node.create(type, name, status, date, location);
        return result;
    }
    
}
