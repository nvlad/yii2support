//package com.nvlad.yii2support.database;
//
//import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
//import com.jetbrains.php.lang.PhpFileType;
//
///**
// * Created by oleg on 05.04.2017.
// */
//public class ActiveQueryNoConnection extends LightCodeInsightFixtureTestCase {
//    protected void setUp() throws Exception {
//        super.setUp();
//        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
//    }
//
//    @Override
//    protected String getTestDataPath() {
//        return "tests/com/nvlad/yii2support/database/fixtures";
//    }
//
//    public void testQuery_Empty() {
//        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
//                "\\test\\PersonModel::find()->where('<caret>");
//        myFixture.completeBasic();
//        assertEquals(3, myFixture.getLookupElementStrings().size());
//    }
//}
