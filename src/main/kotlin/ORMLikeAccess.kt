import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory

object ORMLikeAccess {

    object ModelProvider {
        val model: Model = ModelFactory.createDefaultModel()

        init {
            model.add(
                ResourceFactory.createResource("urn:1"),
                ResourceFactory.createProperty("urn:hasID"),
                ResourceFactory.createTypedLiteral(1)
            )
            model.add(
                ResourceFactory.createResource("urn:1"),
                ResourceFactory.createProperty("urn:propertyA"),
                ResourceFactory.createTypedLiteral("propertyA value")
            )
            model.add(
                ResourceFactory.createResource("urn:1"),
                ResourceFactory.createProperty("urn:propertyB"),
                ResourceFactory.createTypedLiteral(2)
            )
        }
    }

    class MyClass(id: Int) {
        private val propertyA: String
        private val propertyB: Int

        init {
            val values = """
                SELECT ?propertyA ?propertyB
                WHERE {
                    ?x <urn:hasID> $id ;
                       <urn:propertyA> ?propertyA ;
                       <urn:propertyB> ?propertyB .
                }
            """.trimIndent()
                .let { QueryExecutionFactory.create(it, ModelProvider.model) }
                .execSelect()
                .asSequence()
                .first()
            propertyA = values["propertyA"].asLiteral().string
            propertyB = values["propertyB"].asLiteral().int
        }

        override fun toString(): String {
            return "propertyA = $propertyA && porpertyB = $propertyB"
        }
    }


    @JvmStatic
    fun main(args: Array<String>) {
        println(MyClass(1))
    }


}