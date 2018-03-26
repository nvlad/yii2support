package com.nvlad.yii2support.migrations.entities;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Migration implements Comparable<Migration> {
    public PhpClass migrationClass;
    public String name;
    public String path;
    public MigrationStatus status;
    public Date createdAt;

    public Migration(PhpClass clazz, String path) {
        this.migrationClass = clazz;
        this.name = clazz.getName();
        this.path = path;
        this.status = MigrationStatus.Unknown;
        createdAt = getCreatedAt(this.name);
    }

    @Override
    public String toString() {
        return name;
    }

    private static final Pattern dateFromName = Pattern.compile("m(\\d{6}_\\d{6})_.+");
    private static final SimpleDateFormat migrationDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
    private Date getCreatedAt(String name) {
        Matcher matcher = dateFromName.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        try {
            return migrationDateFormat.parse(matcher.group(1));
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public int compareTo(@NotNull Migration migration) {
        return createdAt.compareTo(migration.createdAt);
    }
}
