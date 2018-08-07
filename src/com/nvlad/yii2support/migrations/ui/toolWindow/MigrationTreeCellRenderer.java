package com.nvlad.yii2support.migrations.ui.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.nvlad.yii2support.migrations.entities.DefaultMigrateCommand;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import icons.DatabaseIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;
import java.time.Duration;
import java.util.Enumeration;

class MigrationTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {
    private static final Icon[] progressIcons = {
            AllIcons.RunConfigurations.TestInProgress1,
            AllIcons.RunConfigurations.TestInProgress2,
            AllIcons.RunConfigurations.TestInProgress3,
            AllIcons.RunConfigurations.TestInProgress4,
            AllIcons.RunConfigurations.TestInProgress5,
            AllIcons.RunConfigurations.TestInProgress6,
            AllIcons.RunConfigurations.TestInProgress7,
            AllIcons.RunConfigurations.TestInProgress8,
    };

    @Override
    public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object object = treeNode.getUserObject();
        ColoredTreeCellRenderer renderer = getTextRenderer();
        if (object instanceof String) {
            renderer.setIcon(DatabaseIcons.Catalog);
            renderer.append(treeNode.toString(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);

            Enumeration enumeration = treeNode.children();
            int appliedCount = 0;
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
                Migration migration = (Migration) node.getUserObject();
                if (migration.status == MigrationStatus.Success) {
                    appliedCount++;
                }
            }

            String count = appliedCount + "/" + treeNode.getChildCount() + StringUtil.pluralize(" migration", treeNode.getChildCount());
            renderer.append("  " + count, SimpleTextAttributes.GRAY_ATTRIBUTES, true);
            return;
        }

        if (object instanceof Migration) {
            Migration migration = (Migration) object;
            DefaultMutableTreeNode nodeParent = (DefaultMutableTreeNode) treeNode.getParent();
            final String name;
            if (nodeParent != null && nodeParent.getUserObject() instanceof MigrateCommand) {
                renderer.append((migration.namespace.equals("\\") ? migration.path + "/" : migration.namespace), SimpleTextAttributes.GRAY_ATTRIBUTES, true);
                name =  migration.name;
            } else {
                name = migration.namespace.equals("\\") ? migration.name : migration.namespace + migration.name;
            }
            switch (migration.status) {
                case Progress:
                    renderer.setIcon(getProgressIcon());
                    renderer.append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    if (migration.downDuration != null) {
                        renderer.append("  down time " + formatDuration(migration.downDuration), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                    }
                    break;
                case Unknown:
                    renderer.setIcon(AllIcons.RunConfigurations.Unknown);
                    renderer.append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    break;
                case NotApply:
                    renderer.setIcon(AllIcons.General.Bullet);
                    renderer.append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    if (migration.downDuration != null) {
                        renderer.append("  down time " + formatDuration(migration.downDuration), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                    }
                    break;
                case Success:
                    renderer.setIcon(AllIcons.RunConfigurations.TestPassed);
                    SimpleTextAttributes successAttributes = new SimpleTextAttributes(0, JBColor.green);
                    renderer.append(name, successAttributes, true);

                    if (migration.applyAt != null) {
                        String applyDate = DateFormat.getDateTimeInstance().format(migration.applyAt);
                        renderer.append("  apply at " + applyDate, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                    } else {
                        if (migration.downDuration != null) {
                            renderer.append("  down time " + formatDuration(migration.downDuration), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                        }
                        if (migration.upDuration != null) {
                            renderer.append("  up time " + formatDuration(migration.upDuration), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                        }
                    }
                    break;
                case ApplyError:
                case RollbackError:
                    renderer.setIcon(AllIcons.RunConfigurations.TestError);
                    SimpleTextAttributes errorAttributes = new SimpleTextAttributes(0, JBColor.red);
                    renderer.append(name, errorAttributes, true);
                    break;
            }
        }

        if (object instanceof MigrateCommand) {
            MigrateCommand command = (MigrateCommand) object;
            renderer.setIcon(AllIcons.Debugger.CommandLine);
            renderer.append(command.command, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
        }

        if (object instanceof DefaultMigrateCommand) {
            renderer.append("  applied with ", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
            renderer.append("default", new SimpleTextAttributes(2 | 16, JBColor.GRAY), false);
            renderer.append(" migration command", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
        }
    }

    private String formatDuration(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    private Icon getProgressIcon() {
        int frameIndex = (int) ((System.currentTimeMillis() % 1000) / 125);
        return progressIcons[frameIndex];
    }
}
