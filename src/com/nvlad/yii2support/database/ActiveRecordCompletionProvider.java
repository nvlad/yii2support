package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.PhpReadWriteAccessDetector;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import com.intellij.database.view.DatabaseView;

import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * Created by oleg on 16.02.2017.
 */
public class ActiveRecordCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {


        Project project = completionParameters.getPosition().getProject();
        DatabaseView dbView = DatabaseView.getDatabaseView(project);
        DbPsiFacade facade =  DbPsiFacade.getInstance(project);
        DbDataSource source = (DbDataSource)facade.getDataSources().toArray()[0];
        for (Object item: source.getModel().traverser()) {
            item = item;
            if (item instanceof DbTable) {
                TableInfo tableInfo = new TableInfo((DbTable) item);
                tableInfo = tableInfo;
                DatabaseLookup[] lookups = DatabaseUtils.getLookupItemsByTable("wp_links", project);
                tableInfo = tableInfo;
            }
        }
        dbView = dbView;


    }
}
