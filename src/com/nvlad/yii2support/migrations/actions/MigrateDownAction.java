package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.nvlad.yii2support.migrations.MigrationsToolWindowFactory;
import com.nvlad.yii2support.migrations.commands.MigrationDown;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.ConsolePanel;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateDownAction extends MigrateBaseAction {
    public MigrateDownAction() {
        super("Migrate Down", AllIcons.Actions.Undo);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }

        LinkedList<Migration> migrationsToDown = new LinkedList<>();
        String migrationPath;
        MigrationDown migrationDown = null;

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof String) {
//            MigrationManager manager = MigrationManager.getInstance(project);

            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration migration = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status == MigrationStatus.Success) {
                    migrationsToDown.add(migration);
                }
            }

            migrationPath = (String) userObject;
            migrationDown = new MigrationDown(project, migrationPath, migrationsToDown);

//            migrations = manager.migrateUp((String) userObject, 0);
//            ApplicationManager.getApplication()
//                    .executeOnPooledThread(new MigrationUp(getTree(), manager, (String) userObject, 0));
        }

        if (userObject instanceof Migration) {
            Migration selectedMigration = (Migration) userObject;
            if (selectedMigration.status != MigrationStatus.Success) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.iterator() : migrationList.descendingIterator();
            while (migrationIterator.hasNext()) {
                Migration migration = migrationIterator.next();
                if (migration.status == MigrationStatus.Success) {
                    migrationsToDown.add(migration);
                }

                if (migration == selectedMigration) {
                    break;
                }
            }

            if (migrationsToDown.size() == 0) {
                return;
            }

//            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            migrationPath = (String) ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
            migrationDown = new MigrationDown(project, migrationPath, migrationsToDown);
//            ApplicationManager.getApplication()
//                    .executeOnPooledThread(new MigrationUp(getTree(), manager, (String) parentUserObject, count));
        }


        if (migrationDown != null) {
            ToolWindow window = ToolWindowManager
                    .getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
            if (window != null) {
                Content content = window.getContentManager().getContent(1);
                if (content != null) {
                    ConsolePanel consolePanel = (ConsolePanel) content.getComponent();
                    migrationDown.setConsoleView(consolePanel.getConsoleView());
                }
            }

            migrationDown.repaintComponent(getTree());

            ApplicationManager.getApplication().executeOnPooledThread(migrationDown);
        }

//        DefaultMutableTreeNode treeNode = getSelectedNode();
//        if (treeNode == null) {
//            return;
//        }
//
//        Set<String> migrations = null;
//        Object userObject = treeNode.getUserObject();
//        if (userObject instanceof String) {
//            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
//            migrations = manager.migrateDown((String) userObject, 0);
//        }
//
//        if (userObject instanceof Migration) {
//            Migration migration = (Migration) userObject;
//            if (migration.status != MigrationStatus.Success) {
//                return;
//            }
//
//            LinkedList<Migration> migrationList = new LinkedList<>();
//            Enumeration migrationEnumeration = treeNode.getParent().children();
//            while (migrationEnumeration.hasMoreElements()) {
//                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
//            }
//
//            int count = 0;
//            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
//            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.iterator() : migrationList.descendingIterator();
//            while (migrationIterator.hasNext()) {
//                Migration tmp = migrationIterator.next();
//                if (tmp.status == MigrationStatus.Success) {
//                    count++;
//                }
//
//                if (tmp == migration) {
//                    break;
//                }
//            }
//
//            if (count == 0) {
//                return;
//            }
//
//            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
//            Object parentUserObject = ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
//            migrations = manager.migrateDown((String) parentUserObject, count);
//        }
//
//        if (migrations != null && migrations.size() > 0) {
//            MigrationPanel panel = (MigrationPanel) getContextComponent();
////            panel.updateMigrations();
//        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return false;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status == MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object tmp = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (tmp instanceof Migration) {
                    if (((Migration) tmp).status == MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
