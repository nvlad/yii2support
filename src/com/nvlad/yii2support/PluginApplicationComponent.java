package com.nvlad.yii2support;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.nvlad.yii2support.errorreport.SentryErrorReporter;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.connection.EventSendCallback;
import io.sentry.context.Context;
import io.sentry.event.Event;
import io.sentry.event.UserBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//import com.rollbar.api.payload.Payload;
//import com.rollbar.api.payload.data.Person;
//import com.rollbar.notifier.Rollbar;
//import com.rollbar.notifier.config.Config;
//import com.rollbar.notifier.config.ConfigBuilder;
//import com.rollbar.notifier.sender.listener.SenderListener;
//import com.rollbar.notifier.sender.result.Response;

//import static com.rollbar.notifier.config.ConfigBuilder.withAccessToken;

public class PluginApplicationComponent implements ApplicationComponent {
    private static final PluginId PLUGIN_ID = PluginId.getId("com.yii2support");
    //    private static Rollbar rollbar;
    private final Object lock;

    public PluginApplicationComponent() {
        lock = new Object();
    }

    @Override
    public void initComponent() {
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PLUGIN_ID);
        if (plugin == null) {
            return;
        }

        final PluginGlobalSettings settings = PluginGlobalSettings.getInstance();
        if (StringUtil.isEmpty(settings.uuid)) {
            settings.uuid = UUID.randomUUID().toString();
        }

        if (StringUtil.isEmpty(settings.username)) {
            settings.username = settings.uuid;
        }

        if (!plugin.getVersion().equals(settings.version)) {
            settings.version = plugin.getVersion();

            String popupTitle = plugin.getName() + " v" + plugin.getVersion();
            NotificationGroup group = new NotificationGroup(plugin.getName(), NotificationDisplayType.STICKY_BALLOON, true);
            Notification notification = group.createNotification(
                    popupTitle, plugin.getChangeNotes(), NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER
            );
            Notifications.Bus.notify(notification);
        }
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "PluginApplicationComponent";
    }

    private SubmittedReportInfo reportInfo;

    public void submitErrorReport(Throwable error, String description, Consumer<SubmittedReportInfo> consumer) {
//        Sentry.init("https://e467771bed0c4bb2a39be4c46064d5e5@sentry.io/1271822");
//        Sentry.init(new DefaultSentryClientFactory());
//        Sentry.getContext();
//        Sentry.capture(error);

        SentryClient sentry = SentryClientFactory.sentryClient("https://27bfc50c49954bd49dc69c1fb4ece7fd@sentry.nvlad.com/2");
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
            Map<String, Object> os = new HashMap<>();
            os.put("name", SystemInfo.OS_NAME);
            os.put("version", SystemInfo.OS_VERSION);
            os.put("kernel_version", SystemInfo.OS_ARCH);

            Map<String, Object> runtime = new HashMap<>();
            String ideName = applicationInfo.getBuild().getProductCode().equals("PS") ? "PhpStorm" : "IntelliJ IDEA";
            runtime.put("name", ideName);
            runtime.put("version", applicationInfo.getFullVersion());

            Map<String, Map<String, Object>> contexts = new HashMap<>();
            contexts.put("os", os);
            contexts.put("runtime", runtime);

            eventBuilder.withContexts(contexts);

            if (!StringUtil.isEmptyOrSpaces(description)) {
                eventBuilder.withMessage(description);
            }
        });

        Context context = sentry.getContext();
        context.setUser(new UserBuilder().setId("nvlad").setUsername("NVlad").build());

        context.addTag("plugin.php", SentryErrorReporter.getPluginVersion("com.jetbrains.php"));
        context.addTag("plugin.database", SentryErrorReporter.getPluginVersion("com.intellij.database"));
        context.addTag("plugin.twig", SentryErrorReporter.getPluginVersion("com.jetbrains.twig"));
        context.addTag("plugin.terminal", SentryErrorReporter.getPluginVersion("org.jetbrains.plugins.terminal"));
        context.addTag("phpstorm-remote-interpreter", SentryErrorReporter.getPluginVersion("org.jetbrains.plugins.phpstorm-remote-interpreter"));

        context.addTag("java", SystemInfo.JAVA_RUNTIME_VERSION);

        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PLUGIN_ID);
        if (plugin != null) {
            sentry.setRelease(plugin.getVersion());
        }

        sentry.sendException(error);

//        Rollbar rollbar = getRollbar();
//        Thread thread = new Thread(() -> {
//            synchronized(lock) {
//                reportInfo = null;
//                rollbar.error(error, description);
//                while(reportInfo == null) {
//                    try {
//                        lock.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            consumer.consume(reportInfo);
//        });
//        thread.start();
    }

//    private Rollbar getRollbar() {
//        if (rollbar == null) {
//            Config config = rollbarConfig();
//            config.sender().addListener(new SenderListener() {
//                @Override
//                public void onResponse(Payload payload, Response response) {
//                    synchronized(lock) {
//                        reportInfo = new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
//                        lock.notifyAll();
//                    }
//
//                    if (response.getResult().isError()) {
//                        return;
//                    }
//
//                    showNotification("Report sent. Thanks for contributing.", NotificationType.INFORMATION);
//                }
//
//                @Override
//                public void onError(Payload payload, Exception e) {
//                    synchronized(lock) {
//                        reportInfo = new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED);
//                        lock.notifyAll();
//                    }
//
//                    showNotification("Error report not sent. Try late.", NotificationType.ERROR);
//                }
//
//                private void showNotification(String content, NotificationType notificationType) {
//                    final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PLUGIN_ID);
//                    if (plugin == null) {
//                        return;
//                    }
//
//                    NotificationGroup group = new NotificationGroup(plugin.getName(), NotificationDisplayType.STICKY_BALLOON, true);
//                    Notification notification = group.createNotification(
//                            plugin.getName(), content, notificationType, NotificationListener.URL_OPENING_LISTENER
//                    );
//                    Notifications.Bus.notify(notification);
//                }
//            });
//            rollbar = Rollbar.init(config);
//        }
//
//        return rollbar;
//    }
//
//    private Config rollbarConfig() {
//        final ConfigBuilder config = withAccessToken("469a7c05fd5c438f94f9820923434b39");
//        final PluginGlobalSettings settings = PluginGlobalSettings.getInstance();
//        final ApplicationInfoImpl applicationInfo = (ApplicationInfoImpl) ApplicationInfo.getInstance();
//        String environment = applicationInfo.getApiVersion();
//        environment = environment + " (" + applicationInfo.getFullApplicationName() + ")";
//
//        config
//                .person(() -> new Person.Builder().id(settings.uuid).username(settings.username).build())
//                .environment(environment)
//                .codeVersion("v0.10.56.18")
//                .framework(SystemInfo.JAVA_RUNTIME_VERSION)
//                .platform(SystemInfo.OS_NAME + " " + SystemInfo.OS_VERSION + " (" + SystemInfo.OS_ARCH + ")");
//
//        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PLUGIN_ID);
//        if (plugin == null) {
//            return config.build();
//        }
//
//        config.context(plugin::getVersion);
//
//        return config.build();
//    }

    public static PluginApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(PluginApplicationComponent.class);
    }
}
