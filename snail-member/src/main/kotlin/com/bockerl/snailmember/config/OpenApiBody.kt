package com.bockerl.snailmember.config

import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.core.annotation.AliasFor
import java.lang.annotation.Inherited

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.ANNOTATION_CLASS,
)
@Retention(
    AnnotationRetention.RUNTIME,
)
@RequestBody
@Inherited
annotation class OpenApiBody(
    @get:AliasFor(annotation = RequestBody::class) val description: String = "",
    @get:AliasFor(annotation = RequestBody::class) val content: Array<Content> = [],
    @get:AliasFor(annotation = RequestBody::class) val required: Boolean = false,
    @get:AliasFor(annotation = RequestBody::class) val extensions: Array<Extension> = [],
    @get:AliasFor(annotation = RequestBody::class) val ref: String = "",
    @get:AliasFor(annotation = RequestBody::class) val useParameterTypeSchema: Boolean = false,
)
