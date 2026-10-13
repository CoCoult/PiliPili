package com.PiliPili.annotation;

import com.PiliPili.entity.enums.MessageEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordUserMessage {

    MessageEnum messageType();

}
