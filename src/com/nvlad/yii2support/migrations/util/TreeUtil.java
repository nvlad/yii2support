package com.nvlad.yii2support.migrations.util;

import com.nvlad.yii2support.migrations.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

public class TreeUtil {
    public static void updateTree(JTree tree, Map<MigrateCommand, Collection<Migration>> migrationMap, boolean newestFirst) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (root == null) {
            return;
        }

        boolean first = root.getChildCount() == 0;

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        List<MigrateCommand> commands = new LinkedList<>(migrationMap.keySet());
        commands.sort(new MigrateCommandComparator());

        // Delete removed nodes
        Vector<TreeNode> nodes = new Vector<>();
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            MigrateCommand command = (MigrateCommand) node.getUserObject();
            if (commands.stream().noneMatch(c -> c.isDefault == command.isDefault && c.command.equals(command.command))) {
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
                List<String> paths = new LinkedList<>(migrationTree.keySet());
                paths.sort(String::compareTo);

                cleanDeletedPaths(treeModel, node, paths);

                for (String path : paths) {
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

        Vector<TreeNode> nodes = new Vector<>();
        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enumeration.nextElement();
            Migration nodeObject = (Migration) treeNode.getUserObject();

            if (migrations.stream().noneMatch((m) -> m.migrationClass.equals(nodeObject.migrationClass))) {
                nodes.add(treeNode);
            }
        }

        deleteNodes(treeModel, node, nodes);

        Vector<TreeNode> inserted = new Vector<>();
        Vector<TreeNode> changed = new Vector<>();
        int migrationIndex = 0;
        for (Migration migration : migrations) {
            MutableTreeNode treeNode = findMigrationTreeNode(migration, node);
            if (treeNode == null) {
                treeNode = new DefaultMutableTreeNode(migration);
                node.insert(treeNode, migrationIndex);
                inserted.add(treeNode);
            } else {
                int treeNodeIndex = node.getIndex(treeNode);
                if (treeNodeIndex != migrationIndex) {
                    node.insert(treeNode, migrationIndex);
                }
                changed.add(treeNode);
            }

            migrationIndex++;
        }

        if (inserted.size() > 0) {
            int[] childIndices = new int[inserted.size()];
            for (int i = 0; i < inserted.size(); i++) {
                childIndices[i] = node.getIndex(inserted.get(i));
            }

            treeModel.nodesWereInserted(node, childIndices);
        }

        if (changed.size() > 0) {
            int[] childIndices = new int[changed.size()];
            for (int i = 0; i < changed.size(); i++) {
                childIndices[i] = node.getIndex(changed.get(i));
            }

            treeModel.nodesChanged(node, childIndices);
        }
    }

    @Nullable
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

    @NotNull
    private static DefaultMutableTreeNode getCommandNode(
            DefaultTreeModel treeModel,
            MutableTreeNode root,
            MigrateCommand command,
            int index) {
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
            MigrateCommand migrationCommand = (MigrateCommand) nextElement.getUserObject();
            if (migrationCommand.isDefault == command.isDefault && migrationCommand.command.equals(command.command)) {
                return nextElement;
            }
        }

        DefaultMutableTreeNode result = new DefaultMutableTreeNode(command);
        root.insert(result, index);
        treeModel.nodesWereInserted(root, new int[]{index});

        return result;
    }

    @NotNull
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

    @NotNull
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

    private static void cleanDeletedPaths(DefaultTreeModel treeModel, DefaultMutableTreeNode node, List<String> paths) {
        Vector<TreeNode> nodes = new Vector<>();
        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode checkPathNode = (DefaultMutableTreeNode) enumeration.nextElement();
            if (checkPathNode.getUserObject() instanceof String) {
                if (!paths.contains(checkPathNode.getUserObject().toString())) {
                    nodes.add(checkPathNode);
                }
            }
        }

        deleteNodes(treeModel, node, nodes);
    }

    private static void deleteNodes(DefaultTreeModel treeModel, MutableTreeNode node, List<TreeNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }

        int[] childIndices = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            childIndices[i] = node.getIndex(nodes.get(i));
            node.remove(childIndices[i]);
        }

        treeModel.nodesWereRemoved(node, childIndices, nodes.toArray());
    }
}
