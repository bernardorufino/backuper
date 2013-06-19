package br.com.bernardorufino.labs.backuper.config;

import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Definitions {

    private Definitions() {
        throw new AssertionError("Cannot instantiate object from class " + this.getClass());
    }

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMddHHmmssS");

    public static final String BASE_BACKUP_HEAD = "BASE";

    public static final String MODIFICATIONS_FILE_EXTENSION = "diff";

    public static final String CLIENT_FOLDER_MARKER = "<CLIENT>";

    public static final String INDENTATION = "  ";

    public static final String DELIMITER = " ";

    public static final String DELIMITER_REPLACEMENT = "_";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.mediumDateTime();

    public static final DateTimeComparator dateComparator = DateTimeComparator.getInstance(DateTimeFieldType.secondOfMinute());


}
