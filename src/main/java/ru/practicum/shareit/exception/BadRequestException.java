package ru.practicum.shareit.exception;

import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

public class BadRequestException {
    public BadRequestException(Object object, String objectName, String defaultMessage, Method method)
            throws MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(object, objectName);
        bindingResult.addError(new ObjectError(objectName, defaultMessage));
        MethodParameter parameter = new MethodParameter(method, 0);
        throw new MethodArgumentNotValidException(parameter, bindingResult);
    }
}
