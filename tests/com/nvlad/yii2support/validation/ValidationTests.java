package com.nvlad.yii2support.validation;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.jetbrains.php.lang.PhpFileType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by oleg on 2017-06-11.
 */
public class ValidationTests extends BasePlatformTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("classes.php"));
    }

    @Override
    protected String getTestDataPath() {
        return "tests/com/nvlad/yii2support/validation/fixtures";
    }

    @Test
    public void testCompletionField() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " use yii\\base\\Model; \n" +
                " class ContactForm extends Model {\n" +
                "        public $name;\n" +
                "        public $email;\n" +
                "        public $address;\n" +
                "        public function rules() {\n" +
                "            return [\n" +
                "                [\n" +
                "                    ['name', 'email', 'subject', 'body'], 'required'],\n" +
                "                    ['<caret>']\n" +
                "                ];\n" +
                "        }\n" +
                "    }");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }

    @Test
    public void testCompletionAdditionalField() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " use yii\\base\\Model; \n" +
                " class ContactForm extends Model {\n" +
                "        public $name;\n" +
                "        public $email;\n" +
                "        public $address;\n" +
                "        public function rules() {\n" +
                "            return [\n" +
                "                [\n" +
                "                    ['name', 'email', 'subject', 'body'], 'required'],\n" +
                "                    [['name', '<caret>']]\n" +
                "                ];\n" +
                "        }\n" +
                "    }");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }

    @Test
    public void testCompletionValidators() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " use yii\\base\\Model; \n" +
                " class ContactForm extends Model {\n" +
                "        public $name;\n" +
                "        public $email;\n" +
                "        public function rules() {\n" +
                "            return [\n" +
                "                [\n" +
                "                    ['name', 'email', 'subject', 'body'], 'required'],\n" +
                "                    [['name'], '<caret>']\n" +
                "                ];\n" +
                "        }\n" +
                "       function validateCompany() {}" +
                "    }");
        myFixture.completeBasic();
        assertEquals(2, myFixture.getLookupElementStrings().size());
    }

    @Test
    public void testCompletionValidatorForClassesWithEqualNames() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                "namespace test\\validators {\n" +
                "    class TestValidator extends \\yii\\validators\\Validator\n" +
                "    {\n" +
                "    }\n" +
                "}\n" +
                " use yii\\base\\Model; \n" +
                " class ContactForm extends Model {\n" +
                "        public $name;\n" +
                "        public $email;\n" +
                "        public function rules() {\n" +
                "            return [\n" +
                "                [\n" +
                "                    ['name', 'email', 'subject', 'body'], 'required'],\n" +
                "                    [['name'], '<caret>']\n" +
                "                ];\n" +
                "        }\n" +
                "       function validateCompany() {}" +
                "    }");
        myFixture.completeBasic();
        assertEquals(3, myFixture.getLookupElementStrings().size());
    }

    @Test
    public void testCompletionValidatorParams() {
        myFixture.configureByText(PhpFileType.INSTANCE, "<?php \n" +
                " use yii\\base\\Model; \n" +
                " class ContactForm extends Model {\n" +
                "        public $name;\n" +
                "        public $email;\n" +
                "        public function rules() {\n" +
                "            return [\n" +
                "                [\n" +
                "                    ['name', 'email', 'subject', 'body'], 'required'],\n" +
                "                    [['name'], 'yii\\validators\\TestValidator', '<caret>']\n" +
                "                ];\n" +
                "        }\n" +
                "       function validateCompany() {}" +
                "    }");
        myFixture.completeBasic();
        assertEquals(5, myFixture.getLookupElementStrings().size());
    }
}
