package com.nvlad.yii2support.migrations.ui;

import com.intellij.ui.CheckboxTree;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBFont;
import icons.DatabaseIcons;

import javax.swing.*;
import java.awt.*;

public class MigrationTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {
    private JPanel panel;
    private JLabel primaryText;
    private JLabel secondaryText;

    private final Font plainFont;
    private final Font boldFont;

    MigrationTreeCellRenderer() {
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.ipadx = 4;

        plainFont = panel.getFont();
        boldFont = JBFont.create(plainFont).asBold();

        primaryText = new JLabel();
        panel.add(primaryText);

        secondaryText = new JLabel();
        secondaryText.setForeground(JBColor.foreground().darker());
        panel.add(secondaryText);
    }

    @Override
    public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        getTextRenderer().append("test", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, true);
        getTextRenderer().append("gray", SimpleTextAttributes.GRAY_ATTRIBUTES, true);
        getTextRenderer().setIcon(DatabaseIcons.Catalog);
//        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
//        primaryText.setText(value.toString());
//        secondaryText.setText("");
//        secondaryText.setAlignmentX((float) 0.5);
//
//        Object object = treeNode.getUserObject();
//        if (object instanceof String) {
//            primaryText.setIcon(DatabaseIcons.Catalog);
//            primaryText.setForeground(JBColor.foreground());
//            primaryText.setFont(boldFont);
//
//            String count = treeNode.getChildCount() + StringUtil.pluralize(" migration", treeNode.getChildCount());
//
//            secondaryText.setText("  " + count);
//        }
//
//        if (object instanceof Migration) {
//            Migration migration = (Migration) object;
//            primaryText.setFont(plainFont);
//            switch (migration.status) {
//                case Unknown:
//                    primaryText.setIcon(AllIcons.RunConfigurations.Unknown);
//                    primaryText.setForeground(JBColor.foreground());
//                    break;
//                case NotApply:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestNotRan);
//                    primaryText.setForeground(JBColor.foreground());
//                    break;
//                case Success:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestPassed);
//                    primaryText.setForeground(JBColor.green);
//                    break;
//                case Error:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestError);
//                    primaryText.setForeground(JBColor.red);
//                    break;
//            }
//        }
//
//        return panel;
    }

    //    @Override
//    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
//        primaryText.setText(value.toString());
//        secondaryText.setText("");
//        secondaryText.setAlignmentX((float) 0.5);
//
//        Object object = treeNode.getUserObject();
//        if (object instanceof String) {
//            primaryText.setIcon(DatabaseIcons.Catalog);
//            primaryText.setForeground(JBColor.foreground());
//            primaryText.setFont(boldFont);
//
//            String count = treeNode.getChildCount() + StringUtil.pluralize(" migration", treeNode.getChildCount());
//
//            secondaryText.setText("  " + count);
//        }
//
//        if (object instanceof Migration) {
//            Migration migration = (Migration) object;
//            primaryText.setFont(plainFont);
//            switch (migration.status) {
//                case Unknown:
//                    primaryText.setIcon(AllIcons.RunConfigurations.Unknown);
//                    primaryText.setForeground(JBColor.foreground());
//                    break;
//                case NotApply:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestNotRan);
//                    primaryText.setForeground(JBColor.foreground());
//                    break;
//                case Success:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestPassed);
//                    primaryText.setForeground(JBColor.green);
//                    break;
//                case Error:
//                    primaryText.setIcon(AllIcons.RunConfigurations.TestError);
//                    primaryText.setForeground(JBColor.red);
//                    break;
//            }
//        }
//
//        return panel;
//    }
}
