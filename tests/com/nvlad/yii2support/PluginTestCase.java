package com.nvlad.yii2support;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by oleg on 16.03.2017.
 */
public class PluginTestCase extends LightCodeInsightFixtureTestCase {
    public void assertCompletionResultEquals(String filename, String complete, String result) {
        myFixture.configureByText(filename, complete);
        myFixture.completeBasic();
        myFixture.checkResult(result);
    }
}
