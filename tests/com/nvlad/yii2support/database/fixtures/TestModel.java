//package com.nvlad.yii2support.database.fixtures;
//
//import com.intellij.database.model.*;
//import com.intellij.database.psi.DbElement;
//import com.intellij.database.util.Casing;
//import com.intellij.database.util.DasUtil;
//import com.intellij.openapi.project.Project;
//import com.intellij.util.containers.JBIterable;
//import com.intellij.util.containers.JBTreeTraverser;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * Created by oleg on 03.04.2017.
// */
//public class TestModel implements DasModel {
//    private Project myProject;
//    private TestNamespace myNamespace;
//    private List<DasObject> myList;
//    public TestModel(Project project) {
//        this.myProject = project;
//
//        myList = new LinkedList<>();
//        myNamespace = new TestNamespace();
//
//        TestTable personTestTable = new TestTable("person", myProject);
//        personTestTable.addColumn(new TestColumn(personTestTable, "name", myProject));
//        personTestTable.addColumn(new TestColumn(personTestTable, "surname", myProject));
//        personTestTable.addColumn(new TestColumn(personTestTable, "birth_date", myProject));
//        myList.add(personTestTable);
//        myNamespace.addTable(personTestTable);
//
//        TestTable addressTestTable = new TestTable("address", myProject);
//        addressTestTable.addColumn(new TestColumn(addressTestTable, "street", myProject));
//        addressTestTable.addColumn(new TestColumn(addressTestTable, "city", myProject));
//        myList.add(addressTestTable);
//        myNamespace.addTable(addressTestTable);
//    }
//
//    @NotNull
//    @Override
//    public JBIterable<? extends DasObject> getModelRoots() {
//        List<DasObject> roots = new ArrayList<>(1);
//        roots.add(myNamespace);
//
//        return new JBIterable<DasObject>() {
//            @NotNull
//            @Override
//            public Iterator<DasObject> iterator() {
//                return roots.iterator();
//            }
//        };
//    }
//
//    @Nullable
//    @Override
//    public DasNamespace getCurrentRootNamespace() {
//        return new DasNamespace() {
//            @NotNull
//            @Override
//            public ObjectKind getKind() {
//                return null;
//            }
//
//            @NotNull
//            @Override
//            public String getName() {
//                return null;
//            }
//
//            @Nullable
//            @Override
//            public String getComment() {
//                return null;
//            }
//
//            @Nullable
//            @Override
//            public DasObject getDbParent() {
//                return null;
//            }
//
//            @NotNull
//            @Override
//            public <C> JBIterable<C> getDbChildren(@NotNull Class<C> clazz, @NotNull ObjectKind kind) {
//                return null;
//            }
//        };
//    }
//
//    @NotNull
//    @Override
//    public JBTreeTraverser<DasObject> traverser() {
//        return DasUtil.dasTraverser().withRoots(myList);
////        return new JBTreeTraverser<DasObject>(o -> () -> myList.iterator());
//    }
//
//    @NotNull
//    @Override
//    public JBIterable<? extends DasConstraint> getExportedKeys(DasTable table) {
//        return null;
//    }
//
//    @NotNull
//    @Override
//    public Casing getCasing(@NotNull ObjectKind kind, @Nullable DasObject context) {
//        return null;
//    }
//}
