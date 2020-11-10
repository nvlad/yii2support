package com.nvlad.yii2support.objectfactory;


import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.PhpFileType;
import org.junit.jupiter.api.Test;

/**
 * Created by oleg on 16.03.2017..
 */
public class ObjectFactoryTests extends BasePlatformTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    @Override
    protected String getTestDataPath() {
        return "tests/com/nvlad/yii2support/objectfactory/fixtures";
    }

    @Test
    public void testCompletionWidget_widget() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " \\yii\\base\\TestWidget::widget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }

    @Test
    public void testCompletionWidget_begin() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " \\yii\\base\\TestWidget::begin(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }

    @Test
    public void testCompletionObject_create() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " new \\yii\\base\\TestWidget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }

    @Test
    public void testCompletion_createObject() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " $test = new \\yii\\base\\TestWidget(['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }

    @Test
    public void testCompletionInConfigAndSubObject() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " $test = ['request' => [ 'subobject' => ['<caret>']] ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }

    @Test
    public void testCompletionYii_createObject() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " \\yii\\Yii::createObject('\\yii\\web\\SubObject', ['<caret>']) ;\n" +
                ";");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 3);
    }

    @Test
    public void testCompletionYii_gridColumns() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " \\yii\\grid\\GridView::widget([\n" +
                "    'columns' => [['<caret>']] ");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }

    @Test
    public void testCompletionYii_arrayAsTypedParam() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " (new \\yii\\grid\\GridView())->setColumn(['<caret>']) ");
        myFixture.completeBasic();
        assertEquals(myFixture.getLookupElementStrings().size(), 2);
    }
}
