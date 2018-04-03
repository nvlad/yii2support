package com.nvlad.yii2support.migrations.util;

import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationComparator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationUtil {
    public static void updateTree(JTree tree, Map<String, Collection<Migration>> migrationMap, boolean clear, boolean newestFirst) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (root == null) {
            return;
        }

        if (clear) {
            root.removeAllChildren();
        }

        List<String> paths = new LinkedList<>();
        paths.addAll(migrationMap.keySet());
        paths.sort(String.CASE_INSENSITIVE_ORDER);
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            String nodePath = (String) node.getUserObject();
            if (!paths.contains(nodePath)) {
                node.removeFromParent();
                enumeration = root.children();
            }
        }
        int index = 0;
        for (String path : paths) {
            enumeration = root.children();
            DefaultMutableTreeNode node = null;
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
                String nodePath = (String) nextElement.getUserObject();
                if (nodePath.equals(path)) {
                    node = nextElement;
                    break;
                }
            }
            if (node == null) {
                node = new DefaultMutableTreeNode(path);
                root.insert(node, index);
            }
            index++;

            List<Migration> migrations = new LinkedList<>();
            migrations.addAll(migrationMap.get(path));
            migrations.sort(new MigrationComparator(newestFirst));

            node.removeAllChildren();
            for (Migration migration : migrations) {
                node.add(new DefaultMutableTreeNode(migration));
            }
        }

        tree.updateUI();
    }

    private static final Pattern dateFromName = Pattern.compile("m(\\d{6}_\\d{6})_.+");
    private static final SimpleDateFormat migrationCreateDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");

    @Nullable
    public static Date createDateFromName(String name) {
        Matcher matcher = dateFromName.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        try {
            return migrationCreateDateFormat.parse(matcher.group(1));
        } catch (ParseException e) {
            return null;
        }
    }

    private static final SimpleDateFormat migrationApplyDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Nullable
    public static Date applyDate(String date) {
        try {
            return migrationApplyDateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
