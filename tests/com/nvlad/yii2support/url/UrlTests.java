package com.nvlad.yii2support.url;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testControllerRedirectMethod() {
        List<String> lookupElements = basicCompletionResultsForFile("testControllerRedirectMethod.php");
        List<String> expected = buildFixtureActionList("test/redirect-test");

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testControllerRedirectMethodArrayArgument() {
        List<String> lookupElements = basicCompletionResultsForFile("testControllerRedirectMethodArrayArgument.php");
        List<String> expected = buildFixtureActionList("test/redirect-test");

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testUrlTo() {
        List<String> lookupElements = basicCompletionResultsForFile("testUrlTo.php");
        List<String> expected = buildFixtureActionList();
        expected.add(0,""); // Seems a bug since 2020.3.3

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testUrlParameterCompletion() {
        List<String> lookupElements = basicCompletionResultsForFile("testUrlParameterCompletion.php");

        List<String> expected = new ArrayList<>();
        expected.add("id");
        expected.add("action");
        expected.sort(String::compareTo);

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testUrlToArrayArgument() {
        List<String> lookupElements = basicCompletionResultsForFile("testUrlToArrayArgument.php");
        List<String> expected = buildFixtureActionList();

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testUrlRemember() {
        List<String> lookupElements = basicCompletionResultsForFile("testUrlRemember.php");
        List<String> expected = buildFixtureActionList();
        expected.add(0,""); // Seems a bug since 2020.3.3

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    @Test
    public void testUrlRememberArrayArgument() {
        List<String> lookupElements = basicCompletionResultsForFile("testUrlRememberArrayArgument.php");
        List<String> expected = buildFixtureActionList();

        assertArrayEquals(expected.toArray(), lookupElements.toArray());
    }

    private List<String> buildFixtureActionList() {
        List<String> result = new ArrayList<>();
        result.add("home/index");
        result.add("home/about");
        result.add("home/transactions");
        result.add("home/car-controller");
        result.add("room-controller/index");
        result.add("room-controller/transactions");
        result.add("room-controller/tv-controller");
        result.sort(String::compareTo);

        return result;
    }

    private List<String> buildFixtureActionList(String ...extraActions) {
        List<String> result = buildFixtureActionList();
        Collections.addAll(result, extraActions);
        result.sort(String::compareTo);

        return result;
    }

    private List<String> basicCompletionResultsForFile(String filePath) {
        myFixture.configureByFile(filePath);
        myFixture.completeBasic();

        List<String> lookupElements = myFixture.getLookupElementStrings();
        assertNotNull(lookupElements);
        lookupElements.sort(String::compareTo);

        return lookupElements;
    }
}
