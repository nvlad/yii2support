package com.nvlad.yii2support.database;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.PhpFileType;

import java.io.File;

/**
 * Created by oleg on 05.04.2017.
 */
public class ActiveQueryNoConnection extends LightCodeInsightFixtureTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    @Override
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }


    public void testQuery_Empty() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                "\\test\\PersonModel::find()->where('<caret>");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }
}
