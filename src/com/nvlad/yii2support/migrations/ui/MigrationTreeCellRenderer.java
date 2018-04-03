package com.nvlad.yii2support.migrations.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.nvlad.yii2support.migrations.entities.Migration;
import icons.DatabaseIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.text.DateFormat;

public class MigrationTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {
    @Override
    public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object object = treeNode.getUserObject();
        ColoredTreeCellRenderer renderer = getTextRenderer();
        if (object instanceof String) {
            renderer.setIcon(DatabaseIcons.Catalog);
            renderer.append(treeNode.toString(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);

            String count = "  " + treeNode.getChildCount() + StringUtil.pluralize(" migration", treeNode.getChildCount());
            renderer.append(count, SimpleTextAttributes.GRAY_ATTRIBUTES, true);
            return;
        }

        if (object instanceof Migration) {
            Migration migration = (Migration) object;
            switch (migration.status) {
                case Unknown:
                    renderer.setIcon(AllIcons.RunConfigurations.Unknown);
                    renderer.append(migration.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    break;
                case NotApply:
                    renderer.setIcon(AllIcons.RunConfigurations.TestNotRan);
                    renderer.append(migration.name, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
                    break;
                case Success:
                    renderer.setIcon(AllIcons.RunConfigurations.TestPassed);
                    SimpleTextAttributes successAttributes = new SimpleTextAttributes(0, JBColor.green);
                    renderer.append(migration.name, successAttributes, true);
                    String applyDate = DateFormat.getDateTimeInstance().format(migration.applyAt);
                    renderer.append("  apply at " + applyDate, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES, false);
                    break;
                case Error:
                    renderer.setIcon(AllIcons.RunConfigurations.TestError);
                    SimpleTextAttributes errorAttributes = new SimpleTextAttributes(0, JBColor.red);
                    renderer.append(migration.name, errorAttributes, true);
                    break;
            }
        }
    }
}
