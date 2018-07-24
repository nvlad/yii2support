package com.nvlad.yii2support.migrations.util;

import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationComparator;
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

public class _MigrationUtil {
    public static void updateTree(JTree tree, Map<String, Collection<Migration>> migrationMap, boolean newestFirst) {
//        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
//        if (root == null) {
//            return;
//        }
//
//        boolean first = root.getChildCount() == 0;
//        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
//        List<String> paths = new LinkedList<>(migrationMap.keySet());
//        paths.sort(String.CASE_INSENSITIVE_ORDER);
//
//        Vector<DefaultMutableTreeNode> nodes = new Vector<>();
//        Enumeration enumeration = root.children();
//        while (enumeration.hasMoreElements()) {
//            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
//            String migrationsPath = (String) node.getUserObject();
//            if (!paths.contains(migrationsPath)) {
//                nodes.add(node);
//            }
//        }
//
//        if (nodes.size() > 0) {
//            int[] nodeIndices = new int[nodes.size()];
//            for (int i = 0; i < nodes.size(); i++) {
//                nodeIndices[i] = root.getIndex(nodes.get(i));
//                root.remove(nodeIndices[i]);
//            }
//            treeModel.nodesWereRemoved(root, nodeIndices, nodes.toArray());
//        }
//
//        int index = 0;
//        for (String path : paths) {
//            final MutableTreeNode node = getPathNode(treeModel, root, path, index++);
////            enumeration = root.children();
////            while (enumeration.hasMoreElements()) {
////                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
////                String migrationsPath = (String) nextElement.getUserObject();
////                if (migrationsPath.equals(path)) {
////                    node = nextElement;
////                    break;
////                }
////            }
////
////            if (node == null) {
////                node = new DefaultMutableTreeNode(path);
////                root.insert(node, index);
////                treeModel.nodesWereInserted(root, new int[] {index});
////            }
////            index++;
//
//            List<Migration> migrations = new LinkedList<>(migrationMap.get(path));
//            migrations.sort(new MigrationComparator(newestFirst));
//
//            nodes.clear();
//            Enumeration nodeEnumeration = node.children();
//            while (nodeEnumeration.hasMoreElements()) {
//                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodeEnumeration.nextElement();
//                Migration treeNodeMigration = (Migration) treeNode.getUserObject();
//
//                if (migrations.stream().noneMatch((migration) -> migration.name.equals(treeNodeMigration.name))) {
//                    nodes.add(treeNode);
//                }
//            }
//            if (nodes.size() > 0) {
//                IntStream nodeIndices = nodes.stream().mapToInt(n -> {
//                    int nodeIndex = node.getIndex(n);
//                    node.remove(nodeIndex);
//                    return nodeIndex;
//                });
//                treeModel.nodesWereRemoved(node, nodeIndices.toArray(), nodes.toArray());
//            }
//
//            Vector<Integer> insertIndices = new Vector<>();
//            Vector<Integer> changeIndices = new Vector<>();
//            int migrationIndex = 0;
//            for (Migration migration : migrations) {
//                MutableTreeNode treeNode = findMigrationTreeNode(migration, node);
//
//                if (treeNode == null) {
//                    treeNode = new DefaultMutableTreeNode(migration);
//                    insertIndices.add(migrationIndex);
//                    node.insert(treeNode, migrationIndex);
//                } else {
//                    int treeNodeIndex = node.getIndex(treeNode);
//                    if (treeNodeIndex != migrationIndex) {
////                        insertIndices.add(treeNodeIndex);
////                        insertIndices.add(migrationIndex);
////                        treeModel.nodeChanged(treeNode);
//                        node.insert(treeNode, migrationIndex);
//                    }
//                    changeIndices.add(migrationIndex);
//                }
//
//                migrationIndex++;
//            }
//
//            if (insertIndices.size() > 0) {
//                int[] childIndices = insertIndices.stream().distinct().sorted().mapToInt(v -> v).toArray();
//                treeModel.nodesWereInserted(node, childIndices);
//            }
//
//            if (changeIndices.size() > 0) {
//                treeModel.nodesChanged(node, changeIndices.stream().distinct().sorted().mapToInt(v -> v).toArray());
//            }
//        }
//
//        if (first) {
//            treeModel.nodeStructureChanged(root);
//        }
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

    private static MutableTreeNode getPathNode(DefaultTreeModel treeModel, MutableTreeNode root, String path, int index) {
        Enumeration enumeration = root.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) enumeration.nextElement();
            String migrationsPath = (String) nextElement.getUserObject();
            if (migrationsPath.equals(path)) {
                return nextElement;
            }
        }

        MutableTreeNode result = new DefaultMutableTreeNode(path);
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
}
