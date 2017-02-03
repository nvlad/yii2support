package com.yii2support;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.yii2support.core.Yii;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 03.02.2017.
 */
public class YiiProject implements ProjectComponent {
    @Override
    public void projectOpened() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();

        for (Project project : projects) {
            Yii.applications(project);
        }
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "yii2support";
    }
}
