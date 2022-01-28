package io.kotless.parser.ktor.processor.route.events

import io.kotless.Application
import io.kotless.parser.utils.psi.asString
import io.kotless.parser.utils.psi.getArgument
import io.kotless.resource.Lambda
import io.kotless.utils.TypedStorage
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import kotlin.math.absoluteValue

object SQSEventProcessor : AwsEventProcessor {
    override fun process(
        callExpression: KtCallExpression,
        binding: BindingContext,
        func: KtNamedFunction,
        key: TypedStorage.Key<Lambda>
    ): List<Application.Events.Event> {
        val arn = callExpression.getArgument("queueArn", binding).asString(binding)
        return listOf(
            Application.Events.SQS(
                func.fqName!!.asString().hashCode().absoluteValue.toString(),
                arn,
                key
            )
        )
    }
}
