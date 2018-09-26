package eu.arrowhead.common.json.constraint;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;

/*
  This is very similar in behaviour to the org.hibernate.validator.constraints.NotBlank, but I added a TYPE_USE to the Target constraint, so tis
  constraint can be used in Collection elements, for example List<@SENotBlank String> someList;
 */
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@ReportAsSingleViolation
@NotNull
public @interface SENotBlank {

  String message() default "String element can not be blank!";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
