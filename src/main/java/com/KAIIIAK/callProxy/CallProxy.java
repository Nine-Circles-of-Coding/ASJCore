package com.KAIIIAK.callProxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Class annotated with this can be used to skip adding new methods/fields to existing bytecode for in-IDE calls.
 * Creating `public static PROXY_CLASS c(TARGET_CLASS o)` method inside a proxy class can help with casts if java does not allow it.
 */
@Target(ElementType.TYPE)
public @interface CallProxy {
    String target();
}

