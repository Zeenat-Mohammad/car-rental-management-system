package com.carrental.validation;

import com.carrental.dto.RegistrationForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.HashSet;
import java.util.Set;

@Component
public class RegistrationValidator {

    private static final Class<?>[] ORDER = {
            ValidationGroups.Required.class,
            ValidationGroups.Length.class,
            ValidationGroups.Format.class
    };

    private final Validator validator;

    public RegistrationValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(RegistrationForm form, BindingResult result) {
        Set<String> failedFields = new HashSet<>();

        for (Class<?> group : ORDER) {
            for (ConstraintViolation<RegistrationForm> violation : validator.validate(form, group)) {
                String field = violation.getPropertyPath().toString();
                if (failedFields.add(field)) {
                    String code = violation.getConstraintDescriptor()
                            .getAnnotation().annotationType().getSimpleName();
                    result.rejectValue(field, code, violation.getMessage());
                }
            }
        }
    }
}