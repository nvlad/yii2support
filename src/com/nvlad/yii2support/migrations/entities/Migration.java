package com.nvlad.yii2support.migrations.entities;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Migration implements Comparable<Migration> {
    public PhpClass migrationClass;
    public final String name;
    public final String path;
    public MigrationStatus status;
    public Date createdAt;
    public Date applyAt;
    public Duration downDuration;
    public Duration upDuration;

    public Migration(PhpClass clazz, String path) {
        this.migrationClass = clazz;
        this.name = clazz.getName();
        this.path = path;
        this.status = MigrationStatus.Unknown;
        this.createdAt = dateFromName(this.name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull Migration migration) {
        return createdAt.compareTo(migration.createdAt);
    }

    private static final Pattern dateFromName = Pattern.compile("m(\\d{6}_\\d{6})_.*");
    private static final SimpleDateFormat migrationCreateDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

    @Nullable
    private static Date dateFromName(String name) {
        Matcher matcher = dateFromName.matcher(name);
        if (!matcher.find()) {
            throw new InvalidParameterException("Migration name <" + name + "> not contain or invalid creation date.");
        }

        try {
            return migrationCreateDateFormat.parse(matcher.group(1));
        } catch (ParseException e) {
            throw new InvalidParameterException("Migration name <" + name + "> contain invalid creation date.");
        }
    }
}
