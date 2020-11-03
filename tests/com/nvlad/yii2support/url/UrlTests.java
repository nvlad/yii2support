package com.nvlad.yii2support.url;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.ArrayList;
import java.util.List;

public class UrlTests  extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "tests/com/nvlad/yii2support/url/fixtures";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    public void testControllerRedirectMethod() {
        myFixture.configureByFile("testControllerRedirectMethod.php");
        myFixture.completeBasic();
        List<String> lookupElements = myFixture.getLookupElementStrings();

        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();
        expected.add("test/redirect-test");

        assertContainsElements(lookupElements, expected);
    }

    public void testControllerRedirectMethodArrayArgument() {
        myFixture.configureByFile("testControllerRedirectMethodArrayArgument.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();
        expected.add("test/redirect-test");

        assertContainsElements(lookupElements, expected);
    }

    public void testUrlTo() {
        myFixture.configureByFile("testUrlTo.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();

        assertContainsElements(lookupElements, expected);
    }

    public void testUrlParameterCompletion() {
        myFixture.configureByFile("testUrlParameterCompletion.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = new ArrayList<>();
        expected.add("id");
        expected.add("action");

        assertContainsElements(lookupElements, expected);
    }

    public void testUrlToArrayArgument() {
        myFixture.configureByFile("testUrlToArrayArgument.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();

        assertContainsElements(lookupElements, expected);
    }

    public void testUrlRemember() {
        myFixture.configureByFile("testUrlRemember.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();

        assertContainsElements(lookupElements, expected);
    }

    public void testUrlRememberArrayArgument() {
        myFixture.configureByFile("testUrlRememberArrayArgument.php");
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);

        List<String> expected = buildFixtureActionList();

        assertContainsElements(lookupElements, expected);
    }

    private List<String> buildFixtureActionList() {
        List<String> result = new ArrayList<String>();
        result.add("home/index");
        result.add("home/about");
        result.add("home/transactions");
        result.add("home/car-controller");
        result.add("room-controller/index");
        result.add("room-controller/transactions");
        result.add("room-controller/tv-controller");

        return result;
    }
}
