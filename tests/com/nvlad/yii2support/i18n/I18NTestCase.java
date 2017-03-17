package com.nvlad.yii2support.i18n;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.PhpFileType;

import java.io.File;

/**
 * Created by NVlad on 17.03.2017.
 */
public class I18NTestCase extends LightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("messages/ru/home.php"));
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("messages/ru/layouts.php"));
    }

    public void testCategory() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " Yii::t('<caret>');\n" +
                ";");
        myFixture.completeBasic();
        Object[] result = myFixture.getLookupElementStrings().toArray();
        assertEquals(result.length, 2);
//        assertEquals(result[0], "home");
    }

    public void testMessage() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                "Yii::t('home', '<caret>');");
        myFixture.completeBasic();
        Object[] result = myFixture.getLookupElementStrings().toArray();
        assertEquals(result.length, 2);
    }
}
