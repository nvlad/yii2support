package com.nvlad.yii2support.services;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyKey;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by oleg on 2018-01-25.
 */
public class GlobalCache {
    private static final Logger LOG =
            Logger.getInstance("#yii2support.GlobalCache");

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private static final NotNullLazyKey<GlobalCache, Project> INSTANCE_KEY =
            ServiceManager.createLazyKey(GlobalCache.class);

    private GlobalCache(@Nullable("can be null in com.intellij.core.JavaCoreApplicationEnvironment.JavaCoreApplicationEnvironment") MessageBus messageBus) {
        if (messageBus != null) {
            messageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
                @Override
                public void afterPsiChanged(boolean isPhysical) {
                    if (isPhysical) {
                        clearCaches();
                    }
                }
            });
        }
    }

    public static GlobalCache getInstance(Project project) {
        return INSTANCE_KEY.getValue(project);
    }

    private void clearCaches() {
        cache.clear();
    }

    public void set(String key, Object o) {
        cache.put(key, o);
    }

    public boolean contains(String s) {
        return cache.containsKey(s);
    }

    public Object getValue(String s) {
        return cache.get(s);
    }
}
