package com.nvlad.yii2support.database.fixtures;

import com.intellij.database.model.*;
import com.intellij.database.psi.DbNamespaceImpl;
import com.intellij.database.psi.DbSchemaChildImpl;
import com.intellij.database.util.Casing;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.JBTreeTraverser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oleg on 03.04.2017.
 */
public class TestModel implements DasModel {

    Project project;

    public TestModel(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public JBIterable<? extends DasObject> getModelRoots() {
        return null;
    }

    @Nullable
    @Override
    public DasNamespace getCurrentRootNamespace() {
        return new DasNamespace() {
            @NotNull
            @Override
            public ObjectKind getKind() {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return null;
            }

            @Nullable
            @Override
            public String getComment() {
                return null;
            }

            @Nullable
            @Override
            public DasObject getDbParent() {
                return null;
            }

            @NotNull
            @Override
            public <C> JBIterable<C> getDbChildren(@NotNull Class<C> clazz, @NotNull ObjectKind kind) {
                return null;
            }
        };
    }

    @NotNull
    @Override
    public JBTreeTraverser<DasObject> traverser() {

        List<DasObject> list = new LinkedList<>();
        TestNamespace namespace = new TestNamespace();
        list.add(new TestTable("person", namespace, project));
        list.add(new TestTable("address", namespace, project));
        return new JBTreeTraverser<>(dasObject -> () -> new Iterator<DasObject>() {
            int current = 0;

            @Override
            public boolean hasNext() {
                return list.size() > current;
            }

            @Override
            public DasObject next() {
                return list.get(current++);

            }
        });
    }

    @NotNull
    @Override
    public JBIterable<? extends DasConstraint> getExportedKeys(DasTable table) {
        return null;
    }

    @NotNull
    @Override
    public Casing getCasing(@NotNull ObjectKind kind, @Nullable DasObject context) {
        return null;
    }
}
