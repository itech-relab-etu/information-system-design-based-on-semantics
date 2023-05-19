import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.sparql.syntax.ElementBind
import org.apache.jena.sparql.syntax.ElementFilter
import org.apache.jena.sparql.syntax.ElementGroup
import org.apache.jena.sparql.syntax.ElementOptional
import org.apache.jena.sparql.syntax.ElementUnion

object AnalyseSPARQL {

    @JvmStatic
    fun main(args: Array<String>) {
        val query = """
            PREFIX ex: <http://example.com/>
            SELECT ?x ?y ?z ?m {
                {
                    ?x ex:p1 ?y .
                } UNION {
                    ?x ex:p2 ?y .
                }
                OPTIONAL {?x ex:p3 ?z}
                BIND("static string" AS ?m)
                FILTER(?y > 1)
            }
            GROUP BY ?x ?y ?z ?m
            ORDER BY ?x
            OFFSET 1 LIMIT 2 
        """.trimIndent()
            .let { QueryExecutionFactory.create(it) }
            .query

        println(query.isSelectType)
        println(query.prefixMapping.nsPrefixMap)
        println(query.projectVars)
        val elementGroup = (query.queryPattern as ElementGroup)
        elementGroup.elements
            .forEach{
                when (it::class.java){
                    ElementUnion::class.java -> {
                        val union = it as ElementUnion
                        println(
                            union.elements
                            .joinToString("\n"))
                    }
                    ElementOptional::class.java -> {
                        val optional = it as ElementOptional
                        println(optional.optionalElement)
                    }
                    ElementBind::class.java -> {
                        val bind = it as ElementBind
                        println("${bind.`var`} ~ ${bind.expr}")
                    }
                    ElementFilter::class.java -> {
                        val filter = it as ElementFilter
                        println(filter.expr.function)
                    }
                }

            }
        println(query.groupBy)
        println(query.orderBy)
        println(query.offset)
        println(query.limit)


    }
}