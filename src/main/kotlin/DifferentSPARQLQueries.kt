import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.rdf.model.ModelFactory

object DifferentSPARQLQueries {
    @JvmStatic
    fun main(args: Array<String>) {
        remoteQuery()
        federatedQuery()
    }

    private fun remoteQuery() {
        val sparqlQuery = "SELECT * WHERE { ?s ?p ?o }"
        QueryExecution
            .service("http://localhost:9999/bigdata/namespace/test-graph/sparql", sparqlQuery)
            .execSelect()
            .asSequence()
            .forEach { println("${it["s"]}") }
    }

    private fun federatedQuery(){
        val model = ModelFactory.createDefaultModel()
        """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX p: <http://www.wikidata.org/prop/>
            PREFIX ps: <http://www.wikidata.org/prop/statement/>
            PREFIX wd: <http://www.wikidata.org/entity/>
            SELECT * 
            WHERE {
                SERVICE <http://localhost:9999/bigdata/namespace/test-graph/sparq> {<urn:Protege> a ?c }
                SERVICE <https://query.wikidata.org/sparql> {
                  ?c  rdfs:label ?l1 .
                  ?c  p:P2579/ps:P2579/rdfs:label ?l2 .
                  FILTER(LANG(?l1) = "en" && LANG(?l2) = "en")
                }
            }
        """.trimIndent()
            .let{QueryExecutionFactory.create(it, model)}
            .execSelect()
            .asSequence()
            .forEach { println("Protege is an instance of ${it["l1"]} that is studied bu ${it["l2"]}") }
    }
}