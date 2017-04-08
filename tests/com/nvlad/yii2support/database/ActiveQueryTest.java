package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.jetbrains.php.lang.PhpFileType;
import com.nvlad.yii2support.database.fixtures.TestDataSource;

import java.io.File;
import java.util.List;

/**
 * Created by oleg on 05.04.2017.
 */
public class ActiveQueryTest  extends LightCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));


        DbPsiFacade facade =  DbPsiFacade.getInstance(myFixture.getProject());
        if (facade.getDataSources().size() == 0) {
            TestDataSource source = new TestDataSource(myFixture.getProject());
            facade.getDataSources().add(source);
            List<DbDataSource> dataSources = facade.getDataSources();
        }
    }

    @Override
    protected String getTestDataPath() {
        return new File(this.getClass().getResource("fixtures").getFile()).getAbsolutePath();
    }

    public void testActiveQuery_Empty() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                "\\test\\PersonModel::find()->where('<caret>');");
        myFixture.completeBasic();
        assertEquals(5, myFixture.getLookupElementStrings().size());
    }

    public void testActiveQuery_Prefix() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('per<caret>');");
        myFixture.complete(CompletionType.BASIC);
        assertEquals("'person'", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent().getText());
    }

    public void testActiveQuery_SuffixQuotingDbPrefix() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('{{%per<caret>');");
        myFixture.completeBasic();
        assertEquals("'{{%person'", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent().getText());
    }

    public void testActiveQuery_Columns() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('person.<caret>");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }

    public void testActiveQuery_ColumnPrefix() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('person.na<caret>");
        myFixture.completeBasic();

        assertEquals("name", myFixture.getLookupElementStrings().get(0));
        assertEquals("surname", myFixture.getLookupElementStrings().get(1));
    }

    public void testActiveQuery_TableCompletionWithPrevCondition() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('person.name AND per<caret>");
        myFixture.completeBasic();

        assertEquals("person.name AND person", myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent().getText());

    }

    public void testActiveQuery_TableCompletion() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('<caret>");
        myFixture.completeBasic();

        assertEquals(5, myFixture.getLookupElementStrings().size());

    }

    public void testActiveQuery_ColumnPrefixWithPrevCondition() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('person.surname AND person.na<caret>");
        myFixture.completeBasic();

        assertEquals("name", myFixture.getLookupElementStrings().get(0));
        assertEquals("surname", myFixture.getLookupElementStrings().get(1));
    }

    public void testActiveQuery_ColumnPrefixTableQuoting() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                "var $query = \\test\\PersonModel::find();" +
                "$query->where('{{person}}.na<caret>');");
        myFixture.completeBasic();
        assertEquals("name", myFixture.getLookupElementStrings().get(0));
        assertEquals("surname", myFixture.getLookupElementStrings().get(1));
    }

    public void testActiveQuery_ColumnQuotingAndPrefixTableQuoting() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where('{{person}}.[[na<caret>');");
        myFixture.completeBasic();
        assertEquals("name", myFixture.getLookupElementStrings().get(0));
        assertEquals("surname", myFixture.getLookupElementStrings().get(1));
    }

    public void testActiveQuery_ColumnQuotingAndPrefixTableQuotingDoubleQuote() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " (new \\yii\\db\\Query())->where(\"{{person}}.[[na<caret>\");");
        myFixture.completeBasic();
        assertEquals("name", myFixture.getLookupElementStrings().get(0));
        assertEquals("surname", myFixture.getLookupElementStrings().get(1));
    }

    public void testActiveQuery_ColumnArray() {

        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->where(['<caret>");
        myFixture.completeBasic();

        assertEquals(5, myFixture.getLookupElementStrings().size());
    }

    public void testActiveQuery_HasOneMethod() {
        myFixture.configureByText(PhpFileType.INSTANCE,   "<?php \n" +
                " \\test\\PersonModel::find()->hasOne(AddressModel::class, ['<caret'] )");
        myFixture.completeBasic();

        assertEquals(2, myFixture.getLookupElementStrings().size());
    }
}
