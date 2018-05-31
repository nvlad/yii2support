package com.nvlad.yii2support.views.entities;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.Variable;

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

    @Override
    public int hashCode() {
        return name.hashCode() + type.hashCode() + required.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ViewParameter) {
            ViewParameter parameter = (ViewParameter) obj;
            return parameter.required == this.required
                    && StringUtil.equals(parameter.name, this.name)
                    && StringUtil.equals(parameter.type, this.type);
        }

        return false;
    }
}
