package com.nvlad.yii2support.objectfactory;


import com.jetbrains.php.lang.PhpFileType;
import com.nvlad.yii2support.PluginTestCase;

import java.io.File;


/**
 * Created by oleg on 16.03.2017.
 */
public class ObjectFactoryTests extends PluginTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));

    }

    @Override
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();

    }

    public void testCompletionWidget_widget() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\yii\\base\\TestWidget::widget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

    public void testCompletionWidget_begin() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\yii\\base\\TestWidget::begin(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

    public void testCompletionObject_create() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " new \\yii\\base\\TestWidget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

    public void testCompletionYii_createObject() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " $test = new \\yii\\base\\TestWidget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

    /*
    public void testCompletionInConfig() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " return ['request' => ['<caret>']] ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }
    */
}
