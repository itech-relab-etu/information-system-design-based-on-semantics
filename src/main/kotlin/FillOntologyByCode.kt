import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResourceFactory
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import java.io.StringWriter
import java.util.*

object FillOntologyByCode {
    @JvmStatic
    fun main(args: Array<String>) {
        totallyManual()
        jenaBased()
    }

    private fun jenaBased() {
        val mapper = ObjectMapper()
        val jsonArray = mapper
            .readTree("""[{"field":"value A"},{"field":"valued B"}]""")
        val model = ModelFactory.createDefaultModel()
        jsonArray.asSequence()
            .forEach {
                model.add(
                    ResourceFactory.createResource("http://example.com/${UUID.randomUUID()}"),
                    ResourceFactory.createProperty("http://example.com/hasValue"),
                    ResourceFactory.createPlainLiteral(it["field"].textValue())
                )
            }
        val strWrt = StringWriter()
        RDFDataMgr.write(strWrt, model, Lang.NTRIPLES)
        println(strWrt)
    }

    private fun totallyManual() {
        val mapper = ObjectMapper()
        val jsonArray = mapper
            .readTree("""[{"field":"value A"},{"field":"valued B"}]""")
        jsonArray.asSequence()
            .forEach {
                print("<http://example.com/${UUID.randomUUID()}> ")
                print("<http://example.com/hasValue> ")
                println("${it["field"]} .")
            }
    }
}
