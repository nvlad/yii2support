package com.nvlad.yii2support.common;

import com.intellij.patterns.InitialPatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.injection.PhpElementPattern.Capture;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.Nullable;

/**
 * Created by NVlad on 12.01.2017.
 */
public class Patterns extends PlatformPatterns {
    public static Capture<MethodReference> methodWithName(String... methodNames) {
        return new Capture<>(new InitialPatternCondition<MethodReference>(MethodReference.class) {
            @Override
            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                if (o instanceof MethodReference) {
                    String methodReferenceName = ((MethodReference) o).getName();
                    return methodReferenceName != null && ArrayUtil.contains(methodReferenceName, methodNames);
                }
                return super.accepts(o, context);
            }
        });
    }

    public static Capture<MethodReference> arrayCreation() {
        return new Capture<>(new InitialPatternCondition<MethodReference>(MethodReference.class) {
            @Override
            public boolean accepts(@Nullable Object o, ProcessingContext context) {
                if (o instanceof MethodReference) {
                    return o instanceof ArrayCreationExpression;
                }
                return super.accepts(o, context);
            }
        });
    }
}
