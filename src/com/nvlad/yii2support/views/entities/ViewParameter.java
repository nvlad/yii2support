package com.nvlad.yii2support.views.entities;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;

public class ViewParameter {
    public ViewParameter(Variable variable) {
        this.name = variable.getName();
        this.type = variable.getType().toString();
        this.required = true;
    }

    public ViewParameter(StringLiteralExpression expression) {
    }

    public ViewParameter(String name, String type, Boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String name;
    public String type;
    public Boolean required;
}
