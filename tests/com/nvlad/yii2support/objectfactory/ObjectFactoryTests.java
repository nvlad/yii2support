package com.nvlad.yii2support.objectfactory;


import com.jetbrains.php.lang.PhpFileType;
import com.nvlad.yii2support.PluginTestCase;

import java.io.File;

/**
 * Created by oleg on 16.03.2017..
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

    public void testCompletion_createObject() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " $test = new \\yii\\base\\TestWidget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

    public void testCompletionInConfigAndSubObject() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " $test = ['request' => [ 'subobject' => ['<caret>']] ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 3);
    }

    public void testCompletionYii_createObject() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\yii\\Yii::createObject('\\yii\\web\\SubObject', ['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 3);
    }

    public void testCompletionYii_gridColumns() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\yii\\grid\\GridView::widget([\n" +
                "    'columns' => [['<caret>']] ");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().toArray().length, 2);
    }

}
