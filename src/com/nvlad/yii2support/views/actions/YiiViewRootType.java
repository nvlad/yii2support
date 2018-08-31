package com.nvlad.yii2support.views.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

public class YiiViewRootType extends JpsElementTypeBase<YiiViewProperties> implements JpsModuleSourceRootType<YiiViewProperties> {
    public static final YiiViewRootType VIEW = new YiiViewRootType();

    @NotNull
    @Override
    public YiiViewProperties createDefaultProperties() {
        return new YiiViewProperties("test");
    }
//    public static final JpsModuleSourceRootType<JpsYiiView> VIEW = new YiiViewRootType();
//    public static final JpsElementChildRole<JpsYiiView> ROLE = JpsElementChildRoleBase.create("yii2support_view");
//
//    @NotNull
//    @Override
//    public JpsElementChildRole<JpsYiiView> getPropertiesRole() {
//        return ROLE;
//    }
//
//    @NotNull
//    @Override
//    public JpsYiiView createDefaultProperties() {
//        return new JpsYiiView();
//    }
}
