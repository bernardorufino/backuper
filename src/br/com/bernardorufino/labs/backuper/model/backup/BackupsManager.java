package br.com.bernardorufino.labs.backuper.model.backup;

import br.com.bernardorufino.labs.backuper.config.Definitions;
import br.com.bernardorufino.labs.backuper.model.tree.Node;
import br.com.bernardorufino.labs.backuper.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackupsManager {

    public static boolean isModificationsFile(File file) {
        if (!file.isFile()) return false;
        String pattern = "^(\\d{2}\\d{2}\\d{4}\\d{2}\\d{2}\\d{2}\\d{1,5})\\." + Definitions.MODIFICATIONS_FILE_EXTENSION + "$";
        Matcher m = Pattern.compile(pattern).matcher(file.getName());
        if (!m.matches()) return false;
        try {
            Definitions.DATE_FORMAT.parseDateTime(m.group(1));
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private final File backupsFolder;
    private final File clientsFolder;
    private final NavigableMap<String, Backup> history;

    public BackupsManager(File backupsFolder, File clientsFolder) {
        this.backupsFolder = backupsFolder;
        this.clientsFolder = clientsFolder;
        this.history = new TreeMap<>();
    }

    public void fetchBackups() throws IOException {
        for (File fsNode : backupsFolder.listFiles()) {
            if (!isModificationsFile(fsNode)) continue;
            createFromModificationsFile(fsNode);
        }
    }

    public List<Backup> getBackups() {
        return new ArrayList<>(history.values());
    }

    public Backup createFromModificationsFile(File file) throws IOException {
        String id = Utils.stripExtension(file.getName());
        if (history.containsKey(id)) return history.get(id);
        File backupFolder = getFsNode(id);
        List<String> lines = Utils.readLines(file);
        String head = lines.remove(0);
        String content = StringUtils.join(lines, "\n");
        Node tree = Node.fromList(content, backupFolder);
        Backup backup;
        if (head.equals(Definitions.BASE_BACKUP_HEAD)) {
            backup = new BaseBackup(id, tree, backupFolder, clientsFolder, backupsFolder);
        } else {
            File previousFile = getFsNode(head + "." + Definitions.MODIFICATIONS_FILE_EXTENSION);
            Backup previous = createFromModificationsFile(previousFile);
            backup = new IncrementalBackup(id, previous, tree, backupFolder, clientsFolder, backupsFolder);
        }
        history.put(id, backup);
        return backup;
    }

    public Backup getRecent() {
        return history.lastEntry().getValue();
    }

    private Backup add(Backup backup) {
        history.put(backup.id, backup);
        return backup;
    }

    public Backup makeBackup() throws IOException {
        if (history.size() > 0) {
            return add(new IncrementalBackup(getRecent()));
        } else {
            return add(new BaseBackup(clientsFolder, backupsFolder));
        }
    }

    public void restore(String id) throws IOException {
        history.get(id).restore();
        makeBackup();
    }

    private File getFsNode(String fileName) {
        return backupsFolder.toPath().resolve(fileName).toFile();
    }

}
