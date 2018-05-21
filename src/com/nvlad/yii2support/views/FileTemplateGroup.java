package com.nvlad.yii2support.views;

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import icons.PhpIcons;

import javax.swing.*;

public class FileTemplateGroup implements FileTemplateGroupDescriptorFactory {
    public FileTemplateGroup() {
        System.out.println("public FileTemplateGroup");
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        Icon groupIcon = PhpIcons.Php_icon;
        FileTemplateGroupDescriptor descriptor = new FileTemplateGroupDescriptor("Yii2 Framework", groupIcon);

        descriptor.addTemplate("Yii2 PHP View File.php");
        descriptor.addTemplate("Yii2 Smarty View File.tpl");
        descriptor.addTemplate("Yii2 Twig View File.twig");

        return descriptor;
    }
}
