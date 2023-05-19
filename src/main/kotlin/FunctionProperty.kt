import org.apache.jena.atlas.iterator.Iter
import org.apache.jena.graph.Node
import org.apache.jena.query.ARQ
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.sparql.core.Var
import org.apache.jena.sparql.engine.ExecutionContext
import org.apache.jena.sparql.engine.QueryIterator
import org.apache.jena.sparql.engine.binding.Binding
import org.apache.jena.sparql.engine.binding.BindingFactory
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper
import org.apache.jena.sparql.pfunction.*


object FunctionProperty {

    private val reg: PropertyFunctionRegistry =
        PropertyFunctionRegistry.chooseRegistry(ARQ.getContext())

    private fun init() {
        reg.put("urn:ex:fn#split", SplitFunction())
        PropertyFunctionRegistry.set(ARQ.getContext(), reg)
    }


    class SplitFunction : PropertyFunctionFactory {
        override fun create(uri: String): PropertyFunction {
            return object : PropertyFunctionEval(PropFuncArgType.PF_ARG_EITHER, PropFuncArgType.PF_ARG_EITHER) {
                override fun execEvaluated(
                    parent: Binding?,
                    subject: PropFuncArg?,
                    predicate: Node?,
                    `object`: PropFuncArg?,
                    execCtx: ExecutionContext?
                ): QueryIterator {
                    val strToSplit = subject!!.arg.literal.toString()
                    val delimiter = `object`!!.argList[0].literal.toString()
                    val objectVar = Var.alloc(`object`.argList[1])
                    val tokens = strToSplit.split(delimiter)
                    val it = Iter.map(tokens.iterator()) { item ->
                        BindingFactory.binding(parent, objectVar, ResourceFactory.createPlainLiteral(item).asNode())
                    }
                    return QueryIterPlainWrapper.create(it, execCtx)
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        init()
        val m = ModelFactory.createDefaultModel()
        m.add(
            ResourceFactory.createResource("urn:s"),
            ResourceFactory.createProperty("urn:p"),
            "a,b,c"
        )
        """
            PREFIX fn: <urn:ex:fn#>
            SELECT ?result {
                <urn:s> <urn:p>/fn:split ("," ?result) .
            }
        """.trimIndent()
            .let { QueryExecutionFactory.create(it, m) }
            .execSelect()
            .asSequence()
            .forEach { querySolution -> println("result = ${querySolution["result"]}") }
    }


}