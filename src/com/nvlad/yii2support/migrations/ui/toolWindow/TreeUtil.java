package com.nvlad.yii2support.migrations.ui.toolWindow;

import com.nvlad.yii2support.migrations.entities.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

class TreeUtil {
    static void updateTree(JTree tree, Map<MigrateCommand, Collection<Migration>> migrationMap, boolean newestFirst) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (root == null) {
            return;
        }

        boolean first = root.getChildCount() == 0;

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        List<MigrateCommand> commands = new LinkedList<>(migrationMap.keySet());
        commands.sort(new MigrateCommandComparator());

        // Delete removed nodes
        Vector<DefaultMutableTreeNode> nodes = new Vector<>();
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            MigrateCommand command = (MigrateCommand) node.getUserObject();
            if (!commands.contains(command)) {
                nodes.add(node);
            }
        }

        // Notify UI for updates
        if (nodes.size() > 0) {
            int[] nodeIndices = new int[nodes.size()];
            for (int i = 0; i < nodes.size(); i++) {
                nodeIndices[i] = root.getIndex(nodes.get(i));
                root.remove(nodeIndices[i]);
            }
            treeModel.nodesWereRemoved(root, nodeIndices, nodes.toArray());
        }

        int commandIndex = 0;
        for (MigrateCommand command : commands) {
            if (migrationMap.get(command) == null || migrationMap.get(command).isEmpty()) {
                continue;
            }

            final DefaultMutableTreeNode node = getCommandNode(treeModel, root, command, commandIndex++);
            final List<Migration> migrations = new LinkedList<>(migrationMap.get(command));
            if (node.getUserObject() instanceof DefaultMigrateCommand) {
                Map<String, List<Migration>> migrationTree = buildMigrationPathTree(migrations);

                int pathIndex = 0;
                for (String path : migrationTree.keySet()) {
                    DefaultMutableTreeNode pathNode = getPathNode(treeModel, node, path, pathIndex);
                    addMigrationsToNode(treeModel, pathNode, migrationTree.get(path), newestFirst);
                }

                continue;
            }

            addMigrationsToNode(treeModel, node, migrations, newestFirst);
        }

        if (first) {
            treeModel.nodeStructureChanged(root);
        }
    }

    private static void addMigrationsToNode(DefaultTreeModel treeModel,
                                            DefaultMutableTreeNode node,
                                            List<Migration> migrations,
                                            boolean newestFirst) {
        migrations.sort(new MigrationComparator(newestFirst));
        Vector<DefaultMutableTreeNode> nodes = new Vector<>();
        Enumeration nodeEnumeration = node.children();
        while (nodeEnumeration.hasMoreElements()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodeEnumeration.nextElement();
            Migration treeNodeMigration = (Migration) treeNode.getUserObject();

            if (migrations.stream().noneMatch((migration) -> migration.name.equals(treeNodeMigration.name))) {
                nodes.add(treeNode);
            }
        }

        if (nodes.size() > 0) {
            IntStream nodeIndices = nodes.stream().mapToInt(n -> {
                int nodeIndex = node.getIndex(n);
                node.remove(nodeIndex);
                return nodeIndex;
            });
            treeModel.nodesWereRemoved(node, nodeIndices.toArray(), nodes.toArray());
        }


        Vector<Integer> insertIndices = new Vector<>();
        Vector<Integer> changeIndices = new Vector<>();
        int migrationIndex = 0;
        for (Migration migration : migrations) {
            MutableTreeNode treeNode = findMigrationTreeNode(migration, node);

            if (treeNode == null) {
                treeNode = new DefaultMutableTreeNode(migration);
                insertIndices.add(migrationIndex);
                node.insert(treeNode, migrationIndex);
            } else {
                int treeNodeIndex = node.getIndex(treeNode);
                if (treeNodeIndex != migrationIndex) {
                    node.insert(treeNode, migrationIndex);
                }
                changeIndices.add(migrationIndex);
            }

            migrationIndex++;
        }

        if (insertIndices.size() > 0) {
            int[] childIndices = insertIndices.stream().distinct().sorted().mapToInt(v -> v).toArray();
            treeModel.nodesWereInserted(node, childIndices);
        }

        if (changeIndices.size() > 0) {
            treeModel.nodesChanged(node, changeIndices.stream().distinct().sorted().mapToInt(v -> v).toArray());
        }
    }

    private static MutableTreeNode findMigrationTreeNode(Migration migration, TreeNode node) {
        Enumeration nodeEnumeration = node.children();
        while (nodeEnumeration.hasMoreElements()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodeEnumeration.nextElement();
            Migration treeNodeMigration = (Migration) treeNode.getUserObject();
            if (treeNodeMigration.name.equals(migration.name)) {
                treeNodeMigration.status = migration.status;
                treeNodeMigration.applyAt = migration.applyAt;
                treeNodeMigration.createdAt = migration.createdAt;

                return treeNode;
            }
        }

        return null;
    }

    private static DefaultMutableTreeNode getCommandNode(
            DefaultTreeModel treeModel,
            MutableTreeNode root,
            MigrateCommand command,
            int index) {
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
            MigrateCommand migrationCommand = (MigrateCommand) nextElement.getUserObject();
            if (migrationCommand.equals(command)) {
                return nextElement;
            }
        }

        DefaultMutableTreeNode result = new DefaultMutableTreeNode(command);
        root.insert(result, index);
        treeModel.nodesWereInserted(root, new int[]{index});

        return result;
    }

    private static DefaultMutableTreeNode getPathNode(DefaultTreeModel treeModel, MutableTreeNode root, String path, int index) {
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
            String migrationsPath = (String) nextElement.getUserObject();
            if (migrationsPath.equals(path)) {
                return nextElement;
            }
        }

        DefaultMutableTreeNode result = new DefaultMutableTreeNode(path);
        root.insert(result, index);
        treeModel.nodesWereInserted(root, new int[]{index});

        return result;
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

    private static Map<String, List<Migration>> buildMigrationPathTree(List<Migration> migrations) {
        Map<String, List<Migration>> result = new HashMap<>();
        for (Migration migration : migrations) {
            if (!result.containsKey(migration.path)) {
                result.put(migration.path, new LinkedList<>());
            }

            result.get(migration.path).add(migration);
        }

        return result;
    }
}
