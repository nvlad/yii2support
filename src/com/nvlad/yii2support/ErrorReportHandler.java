package com.nvlad.yii2support;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import com.rollbar.notifier.Rollbar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static com.rollbar.notifier.config.ConfigBuilder.withAccessToken;

public class ErrorReportHandler extends ErrorReportSubmitter {
    private static final Rollbar rollbar = Rollbar.init(withAccessToken("7b5e3f2f1e3d4084869f7fff29f87688").build());

    @Override
    public String getReportActionText() {
        return "Send to Yii2 Support";
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
        for (IdeaLoggingEvent event : events) {
            rollbar.error(event.getThrowable(), event.getMessage());
        }

        return true;
    }
}
