package com.nvlad.yii2support.migrations.util;

import com.nvlad.yii2support.migrations.entities.Migration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collection;
import java.util.Map;

public class MigrationUtil {
    public static void updateTree(JTree tree, Map<String, Collection<Migration>> migrationMap) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (root == null) {
            return;
        }

        root.removeAllChildren();
        for (Map.Entry<String, Collection<Migration>> entry : migrationMap.entrySet()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getKey());
            root.add(node);

            for (Migration migration : entry.getValue()) {
                node.add(new DefaultMutableTreeNode(migration));
            }
        }
        tree.updateUI();
    }
}
