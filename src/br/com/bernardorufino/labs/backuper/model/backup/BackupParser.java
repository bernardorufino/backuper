package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.libs.Utils;
import br.com.bernardorufino.labs.backuper.model.tree.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.bernardorufino.labs.backuper.config.Definitions.*;

public class BackupParser {

    private static final Pattern CLIENT_PATTERN = Pattern.compile("^" + Pattern.quote(CLIENT_FOLDER_MARKER) +
                                                                  DELIMITER + "(.*)$");

    public static boolean isModificationsFile(File file) {
        if (!file.isFile()) return false;
        String pattern = "^(\\d{2}\\d{2}\\d{4}\\d{2}\\d{2}\\d{2}\\d{1,5})\\." + MODIFICATIONS_FILE_EXTENSION + "$";
        Matcher m = Pattern.compile(pattern).matcher(file.getName());
        return m.matches();
    }

    public static Backup buildInHistory(File file, NavigableMap<String, Backup> history) throws IOException {
        String id = Utils.stripExtension(file.getName());
        if (history.containsKey(id)) return history.get(id);
        File backupsFolder = file.getParentFile();
        File backupFolder = Utils.getFsNode(backupsFolder, id);
        List<String> lines = Utils.readLines(file);
        String head = lines.remove(0);
        Map<File, Node> trees = buildTrees(lines, backupFolder);
        Backup backup;
        if (head.equals(BASE_BACKUP_HEAD)) {
            backup = new BaseBackup(id, trees, backupFolder);
        } else {
            File previousFile = Utils.getFsNode(backupsFolder, head + "." + MODIFICATIONS_FILE_EXTENSION);
            Backup previous = buildInHistory(previousFile, history);
            backup = new IncrementalBackup(id, previous, trees, backupFolder);
        }
        history.put(id, backup);
        return backup;
    }

    public static Map<File, Node> buildTrees(List<String> lines, File backupFolder) throws IOException {
        Map<File, Node> trees = new LinkedHashMap<>();
        StringBuilder content = new StringBuilder();
        if (lines.size() < 1) return trees;
        Matcher match = CLIENT_PATTERN.matcher(lines.remove(0));
        if (!match.matches()) return null;
        String clientPath = match.group(1);
        File clientFolder = new File(clientPath);
        for (Iterator<String> i = lines.iterator(); i.hasNext(); ) {
            String line = i.next();
            match = CLIENT_PATTERN.matcher(line);
            if (match.matches() || !i.hasNext()) {
                // Append the last line before building the tree
                if (!i.hasNext()) { content.append(line); }
                Node tree = Node.fromList(content.toString(), backupFolder);
                // clientFolder is the previous one, so put in the mao before updating it
                trees.put(clientFolder, tree);
                if (match.matches()) {
                    clientFolder = new File(match.group(1));
                    // Resets the buffer
                    content.setLength(0);
                }
            } else {
                content.append(line).append("\n");
            }
        }
        return trees;
    }

}
