package com.KAIIIAK.superwrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Эта аннотация сгенерирует новый метод в целевом классе, который будет вызывать
 * метод конкретно целевого класса (игнорируя переопределение в классах-наследниках)
 * <br>
 * Целевой класс указывается первым аргументом метода с этой аннотацией, остальные
 * аргументы должны совпадать со списком и порядком аргументов вызываемого метода
 * <br>
 * Метод с аннотацией обязательно должен быть <i>static</i>
 * а его содержимое будет заменено вызовом сгенерированного метода.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuperWrapper {
	
	/**
	 * Имя целевого метода, который будет вызван<br>
	 * Если не указан, будет идентичен имени метода с этой аннотацией
	 */
	String targetMethod() default "";
	
	/**
	 * Префикс в названии генерируемого метода<br>
	 * Если не указан, будет "Super__"
	 */
	String methodNamePrefix() default "";
	
	/**
	 * Префикс в названии генерируемого метода<br>
	 * Если не указан, будет "__Wrapper"
	 */
	String methodNamePostfix() default "";
	
	/**
	 * Список исключений, которые могут быть выброшены при вызове метода<br>
	 * Иными словами, список того, что пишется после throws<br>
	 * Если не указан, будет идентичен списку throws метода с этой аннотацией
	 */
	String[] exceptions() default {};
	
	/**
	 * Сигнатура генерируемого метода<br>
	 * Если не указана, будет идентична сигнатуре метода с этой аннотацией
	 */
	String signature() default "";
 
}
