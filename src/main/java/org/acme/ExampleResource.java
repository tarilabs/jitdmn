package org.acme;

import java.io.StringReader;
import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.model.api.Definitions;
import org.kie.internal.io.ResourceFactory;

@Path("/jitdmn")
public class ExampleResource {

    // @POST
    // @Produces(MediaType.TEXT_PLAIN)
    // public String jitdmn(JITDMNPayload payload) {
    // String modelXML = payload.getModel();
    // System.out.println(modelXML);
    // Definitions unmarshal =
    // DMNMarshallerFactory.newDefaultMarshaller().unmarshal(new
    // StringReader(payload.getModel()));
    // return unmarshal.getName();
    // }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String jitdmn(JITDMNPayload payload) throws Exception {
        String modelXML = payload.getModel();
        System.out.println(modelXML);
        Resource modelResource = ResourceFactory.newReaderResource(new StringReader(modelXML), "UTF-8");
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(Arrays.asList(modelResource)).getOrElseThrow(RuntimeException::new);
        DMNModel dmnModel = dmnRuntime.getModels().get(0);
        DMNContext dmnContext = dmnRuntime.newContext();
        payload.getContext().forEach((k, v) -> dmnContext.set(k, v));
        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        String result = new ObjectMapper().writeValueAsString(evaluateAll.getContext().getAll()); // TODO will need to stub out non-serializable functions.
        System.out.println(result);
        return result;
    }
}