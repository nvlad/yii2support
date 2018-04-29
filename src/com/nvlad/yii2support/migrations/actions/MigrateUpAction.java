package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.MigrationUp;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateUpAction extends MigrateBaseAction {
    public MigrateUpAction() {
        super("Migrate Up", AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }

        LinkedList<Migration> migrationsToUp = new LinkedList<>();
        String migrationPath;
        MigrationUp migrationUp = null;

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
                if (migration.status != MigrationStatus.Success) {
                    migrationsToUp.add(migration);
                }
            }

            migrationPath = (String) userObject;
            migrationUp = new MigrationUp(project, migrationPath, migrationsToUp);

//            migrations = manager.migrateUp((String) userObject, 0);
//            ApplicationManager.getApplication()
//                    .executeOnPooledThread(new MigrationUp(getTree(), manager, (String) userObject, 0));
        }

        if (userObject instanceof Migration) {
            Migration selectedMigration = (Migration) userObject;
            if (selectedMigration.status == MigrationStatus.Success) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.descendingIterator() : migrationList.iterator();
            while (migrationIterator.hasNext()) {
                Migration migration = migrationIterator.next();
                if (migration.status != MigrationStatus.Success) {
                    migrationsToUp.add(migration);
                }

                if (migration == selectedMigration) {
                    break;
                }
            }

            if (migrationsToUp.size() == 0) {
                return;
            }

//            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            migrationPath = (String) ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
            migrationUp = new MigrationUp(project, migrationPath, migrationsToUp);
//            ApplicationManager.getApplication()
//                    .executeOnPooledThread(new MigrationUp(getTree(), manager, (String) parentUserObject, count));
        }


        if (migrationUp != null) {
            executeCommand(project, migrationUp);
        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return false;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status != MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object tmp = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (tmp instanceof Migration) {
                    if (((Migration) tmp).status != MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

//    class MigrationUp implements Runnable {
//        private final JTree myTree;
//        private final MigrationManager myManager;
//        private final String myPath;
//        private final int myCount;
//        private Alarm myAlarm;
//
//        MigrationUp(JTree tree, MigrationManager manager, String path, int count) {
//            myTree = tree;
//            myManager = manager;
//            myPath = path;
//            myCount = count;
//            myAlarm = new Alarm();
//        }
//
//        @Override
//        public void run() {
//            myAlarm.addRequest(this::repaintTree, 125);
//            Set<String> migrations = myManager.migrateUp(myPath, myCount);
//            myAlarm.cancelAllRequests();
//
//            if(migrations != null && migrations.size() > 0) {
//                MigrationPanel panel = (MigrationPanel) getContextComponent();
//                panel.updateMigrations();
//            }
//        }
//
//        void repaintTree() {
//            myAlarm.addRequest(this::repaintTree, 125);
//            myTree.updateUI();
//        }
//    }
}
