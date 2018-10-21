package com.nvlad.yii2support.errorreport;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.nvlad.yii2support.PluginApplicationComponent;
import com.nvlad.yii2support.PluginGlobalSettings;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.connection.EventSendCallback;
import io.sentry.context.Context;
import io.sentry.event.Event;
import io.sentry.event.UserBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class SentryErrorReporter {
    static void submitErrorReport(Throwable error, String description, Consumer<SubmittedReportInfo> consumer) {
        final SentryClient sentry = SentryClientFactory.sentryClient("https://27bfc50c49954bd49dc69c1fb4ece7fd@sentry.nvlad.com/2");
        sentry.addEventSendCallback(new EventSendCallback() {
            @Override
            public void onFailure(Event event, Exception exception) {
                consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED));
            }

            @Override
            public void onSuccess(Event event) {
                consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
            }
        });

        final ApplicationInfoImpl applicationInfo = (ApplicationInfoImpl) ApplicationInfo.getInstance();
        sentry.addBuilderHelper(eventBuilder -> {
            final Map<String, Object> os = new HashMap<>();
            os.put("name", SystemInfo.OS_NAME);
            os.put("version", SystemInfo.OS_VERSION);
            os.put("kernel_version", SystemInfo.OS_ARCH);

            final Map<String, Object> runtime = new HashMap<>();
            final String ideName = applicationInfo.getBuild().getProductCode().equals("PS") ? "PhpStorm" : "IntelliJ IDEA";
            runtime.put("name", ideName);
            runtime.put("version", applicationInfo.getFullVersion());

            final Map<String, Map<String, Object>> contexts = new HashMap<>();
            contexts.put("os", os);
            contexts.put("runtime", runtime);
            SentryErrorReporter.fillActivePlugins(contexts);

            eventBuilder.withContexts(contexts);

            if (!StringUtil.isEmptyOrSpaces(description)) {
                eventBuilder.withMessage(description);
                eventBuilder.withTag("with-description", "true");
            }
        });

        final PluginGlobalSettings settings = PluginGlobalSettings.getInstance();
        final Context context = sentry.getContext();
        context.setUser(new UserBuilder().setId(settings.uuid).setUsername(settings.username).build());

        context.addTag("plugin.php", SentryErrorReporter.getPluginVersion("com.jetbrains.php"));
        context.addTag("plugin.database", SentryErrorReporter.getPluginVersion("com.intellij.database"));
        context.addTag("plugin.twig", SentryErrorReporter.getPluginVersion("com.jetbrains.twig"));
        context.addTag("plugin.terminal", SentryErrorReporter.getPluginVersion("org.jetbrains.plugins.terminal"));
        context.addTag("phpstorm-remote-interpreter", SentryErrorReporter.getPluginVersion("org.jetbrains.plugins.phpstorm-remote-interpreter"));

        context.addTag("java", SystemInfo.JAVA_RUNTIME_VERSION);

        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginApplicationComponent.PLUGIN_ID);
        if (plugin != null) {
            sentry.setRelease(plugin.getVersion());
        }

        sentry.sendException(error);
    }

    @Nullable
    private static String getPluginVersion(String pluginKey) {
        final PluginId pluginId = PluginId.getId(pluginKey);
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginId);
        if (plugin == null) {
            return null;
        }

        return plugin.getVersion();
    }

    private static void fillActivePlugins(Map<String, Map<String, Object>> contexts) {
        final Map<String, Object> bundledPlugins = new HashMap<>();
        final Map<String, Object> activePlugins = new HashMap<>();
        for (IdeaPluginDescriptor plugin : PluginManager.getPlugins()) {
            if (!plugin.isEnabled()) {
                continue;
            }

            if (plugin.isBundled()) {
                bundledPlugins.put(plugin.getName(), plugin.getVersion());
            } else {
                activePlugins.put(plugin.getName(), plugin.getVersion());
            }
        }

        if (!bundledPlugins.isEmpty()) {
            contexts.put("bundled plugins", bundledPlugins);
        }

        if (!activePlugins.isEmpty()) {
            contexts.put("active plugins", activePlugins);
        }
    }
}
