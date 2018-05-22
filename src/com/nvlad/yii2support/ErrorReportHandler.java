package com.nvlad.yii2support;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ErrorReportHandler extends ErrorReportSubmitter {

    @Override
    public String getReportActionText() {
        return "Send to Yii2 Support developers";
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
        for (IdeaLoggingEvent event : events) {
            PluginApplicationComponent.getInstance().getRollbar().error(event.getThrowable(), additionalInfo);
        }

        return true;
    }
}
