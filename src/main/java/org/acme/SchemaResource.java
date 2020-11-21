package org.acme;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.DMNType;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.openapi.DMNOASGeneratorFactory;
import org.kie.dmn.openapi.model.DMNOASResult;
import org.kie.internal.io.ResourceFactory;

import io.smallrye.openapi.runtime.io.schema.SchemaConstant;
import io.smallrye.openapi.runtime.io.schema.SchemaWriter;

@Path("/schema")
public class SchemaResource {

    // TODO trick for resolver/implementation for NI
    private static final OpenAPI x = OASFactory.createObject(OpenAPI.class);

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public String jitdmn(String payload) throws Exception {
        String modelXML = payload;
        System.out.println(modelXML);
        Resource modelResource = ResourceFactory.newReaderResource(new StringReader(modelXML), "UTF-8");
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(Arrays.asList(modelResource)).getOrElseThrow(RuntimeException::new);
        DMNModel dmnModel = dmnRuntime.getModels().get(0);

        DMNOASResult oasResult = DMNOASGeneratorFactory.generator(Arrays.asList(dmnModel)).build();
        ObjectNode jsNode = oasResult.getJsonSchemaNode();
        
        DMNType is = oasResult.lookupIOSetsByModel(dmnModel).getInputSet();
        String isRef = oasResult.getNamingPolicy().getRef(is);
        Schema schema = OASFactory.createObject(Schema.class).type(SchemaType.OBJECT);
        schema.addProperty("context", OASFactory.createObject(Schema.class).type(SchemaType.OBJECT).ref(isRef));
        schema.addProperty("model", OASFactory.createObject(Schema.class).type(SchemaType.STRING));
        ObjectNode schemasNode = jsNode.putObject("properties");
        for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
            SchemaWriter.writeSchema(schemasNode, entry.getValue(), entry.getKey());
        }
        jsNode.put("type", "object");
        jsNode.putArray("required").add("context").add("model");
        
        String jsonContent = new ObjectMapper().writeValueAsString(jsNode);
        return jsonContent;
    }
}