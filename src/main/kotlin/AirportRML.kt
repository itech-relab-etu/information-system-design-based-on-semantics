import io.carml.engine.rdf.RdfRmlMapper
import io.carml.logicalsourceresolver.CsvResolver
import io.carml.logicalsourceresolver.JsonPathResolver
import io.carml.logicalsourceresolver.XPathResolver
import io.carml.util.RmlMappingLoader
import io.carml.util.jena.JenaCollectors
import io.carml.util.jena.JenaConverters
import io.carml.vocab.Rdf
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.eclipse.rdf4j.model.Statement
import org.eclipse.rdf4j.rio.RDFFormat
import reactor.core.publisher.Flux
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.Paths


object AirportRML {
    @JvmStatic
    fun main(args: Array<String>) {
        val mapping = RmlMappingLoader.build()
            .load(RDFFormat.TURTLE, Paths.get(
                AirportRML::class.java.classLoader.getResource("mappings/Airport.rml")!!.path)
            )
        val mapper = RdfRmlMapper.builder()
            // add mappings
            .triplesMaps(mapping)
            // Add the resolvers to suit your need
            .setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance)
            .setLogicalSourceResolver(Rdf.Ql.XPath, XPathResolver::getInstance)
            .setLogicalSourceResolver(Rdf.Ql.Csv, CsvResolver::getInstance)
            //-- optional: --
            // specify base IRI to use for relative IRIs in mapping results
            // default is "http://example.com/base/"
            .baseIri("http://example.com/")
            // set file directory for sources in mapping
            .fileResolver(
                Path.of("./src/main/resources/data")
            )
            //---------------
            .build()
        val statements: Flux<Statement> = mapper.map()
        val jenaDataset = statements
            .map { statement: Statement? -> JenaConverters.toQuad(statement!!) }
            .collect(JenaCollectors.toDataset())
            .block()
        val strWrt = StringWriter()
        RDFDataMgr.write(strWrt, jenaDataset, Lang.NQ)
        println(strWrt)
    }
}