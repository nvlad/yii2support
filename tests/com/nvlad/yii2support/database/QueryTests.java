package com.nvlad.yii2support.database;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.jetbrains.php.lang.PhpFileType;
import com.nvlad.yii2support.PluginTestCase;
import com.nvlad.yii2support.database.fixtures.TestDataSource;

import java.io.File;
import java.util.List;

/**
 * Created by oleg on 03.04.2017.
 */
public class QueryTests extends PluginTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));

        DbPsiFacade facade =  DbPsiFacade.getInstance(myFixture.getProject());
        TestDataSource source = new TestDataSource(myFixture.getProject());
        facade.getDataSources().add(source);
        List<DbDataSource> dataSources = facade.getDataSources();
        dataSources = dataSources;
    }

    @Override
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testQuery_Empty() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " (new \\yii\\db\\Query())->where('<caret>')\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

}
