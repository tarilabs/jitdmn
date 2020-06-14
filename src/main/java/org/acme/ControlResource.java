package org.acme;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.backend.marshalling.CustomStaxReader;
import org.kie.dmn.backend.marshalling.v1_2.xstream.XStreamMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.model.api.Definitions;
import org.kie.internal.io.ResourceFactory;
import org.kie.soup.xstream.XStreamUtils;

@Path("/control")
public class ControlResource {
    private static final String MODEL = "<?xml version=\"1.0\" ?>\r\n<dmn:definitions xmlns:dmn=\"http://www.omg.org/spec/DMN/20180521/MODEL/\" xmlns=\"xls2dmn_4f6c1154-6315-413b-9976-f1a81d9f6ef9\" xmlns:di=\"http://www.omg.org/spec/DMN/20180521/DI/\" xmlns:feel=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" xmlns:dmndi=\"http://www.omg.org/spec/DMN/20180521/DMNDI/\" xmlns:dc=\"http://www.omg.org/spec/DMN/20180521/DC/\" name=\"xls2dmn\" expressionLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" typeLanguage=\"http://www.omg.org/spec/DMN/20180521/FEEL/\" namespace=\"xls2dmn_4f6c1154-6315-413b-9976-f1a81d9f6ef9\" exporter=\"kie-dmn-xls2dmn\">\r\n  <dmn:inputData id=\"id_FICO_32Score\" name=\"FICO Score\">\r\n    <dmn:variable id=\"idvar_FICO_32Score\" name=\"FICO Score\"></dmn:variable>\r\n  </dmn:inputData>\r\n  <dmn:inputData id=\"id_DTI_32Ratio\" name=\"DTI Ratio\">\r\n    <dmn:variable id=\"idvar_DTI_32Ratio\" name=\"DTI Ratio\"></dmn:variable>\r\n  </dmn:inputData>\r\n  <dmn:inputData id=\"id_PITI_32Ratio\" name=\"PITI Ratio\">\r\n    <dmn:variable id=\"idvar_PITI_32Ratio\" name=\"PITI Ratio\"></dmn:variable>\r\n  </dmn:inputData>\r\n  <dmn:decision id=\"d_Loan_32Approval\" name=\"Loan Approval\">\r\n    <dmn:variable id=\"dvar_Loan_32Approval\" name=\"Loan Approval\"></dmn:variable>\r\n    <dmn:informationRequirement>\r\n      <dmn:requiredInput href=\"#id_FICO_32Score\"></dmn:requiredInput>\r\n    </dmn:informationRequirement>\r\n    <dmn:informationRequirement>\r\n      <dmn:requiredDecision href=\"#d_DTI_32Rating\"></dmn:requiredDecision>\r\n    </dmn:informationRequirement>\r\n    <dmn:informationRequirement>\r\n      <dmn:requiredDecision href=\"#d_PITI_32Rating\"></dmn:requiredDecision>\r\n    </dmn:informationRequirement>\r\n    <dmn:decisionTable id=\"ddt_Loan_32Approval\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"Loan Approval\">\r\n      <dmn:input label=\"FICO Score\">\r\n        <dmn:inputExpression>\r\n          <dmn:text>FICO Score</dmn:text>\r\n        </dmn:inputExpression>\r\n      </dmn:input>\r\n      <dmn:input label=\"DTI Rating\">\r\n        <dmn:inputExpression>\r\n          <dmn:text>DTI Rating</dmn:text>\r\n        </dmn:inputExpression>\r\n      </dmn:input>\r\n      <dmn:input label=\"PITI Rating\">\r\n        <dmn:inputExpression>\r\n          <dmn:text>PITI Rating</dmn:text>\r\n        </dmn:inputExpression>\r\n      </dmn:input>\r\n      <dmn:output></dmn:output>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&lt;=750</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Not approved\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>\"Bad\"</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Not approved\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>-</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>\"Bad\"</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Not approved\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&gt;750</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>\"Good\"</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:inputEntry>\r\n          <dmn:text>\"Good\"</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Approved\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n    </dmn:decisionTable>\r\n  </dmn:decision>\r\n  <dmn:decision id=\"d_DTI_32Rating\" name=\"DTI Rating\">\r\n    <dmn:variable id=\"dvar_DTI_32Rating\" name=\"DTI Rating\"></dmn:variable>\r\n    <dmn:informationRequirement>\r\n      <dmn:requiredInput href=\"#id_DTI_32Ratio\"></dmn:requiredInput>\r\n    </dmn:informationRequirement>\r\n    <dmn:decisionTable id=\"ddt_DTI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"DTI Rating\">\r\n      <dmn:input label=\"DTI Ratio\">\r\n        <dmn:inputExpression>\r\n          <dmn:text>DTI Ratio</dmn:text>\r\n        </dmn:inputExpression>\r\n      </dmn:input>\r\n      <dmn:output></dmn:output>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&lt;=0.20</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Good\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&gt;0.20</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Bad\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n    </dmn:decisionTable>\r\n  </dmn:decision>\r\n  <dmn:decision id=\"d_PITI_32Rating\" name=\"PITI Rating\">\r\n    <dmn:variable id=\"dvar_PITI_32Rating\" name=\"PITI Rating\"></dmn:variable>\r\n    <dmn:informationRequirement>\r\n      <dmn:requiredInput href=\"#id_PITI_32Ratio\"></dmn:requiredInput>\r\n    </dmn:informationRequirement>\r\n    <dmn:decisionTable id=\"ddt_PITI_32Rating\" hitPolicy=\"ANY\" preferredOrientation=\"Rule-as-Row\" outputLabel=\"PITI Rating\">\r\n      <dmn:input label=\"PITI Ratio\">\r\n        <dmn:inputExpression>\r\n          <dmn:text>PITI Ratio</dmn:text>\r\n        </dmn:inputExpression>\r\n      </dmn:input>\r\n      <dmn:output></dmn:output>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&lt;=0.28</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Good\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n      <dmn:rule>\r\n        <dmn:inputEntry>\r\n          <dmn:text>&gt;0.28</dmn:text>\r\n        </dmn:inputEntry>\r\n        <dmn:outputEntry>\r\n          <dmn:text>\"Bad\"</dmn:text>\r\n        </dmn:outputEntry>\r\n      </dmn:rule>\r\n    </dmn:decisionTable>\r\n  </dmn:decision>\r\n</dmn:definitions>\r\n";

    public static DMNRuntime dmnRuntime;
    public static XStream x = XStreamUtils.createTrustingXStream();
    public static XStreamMarshaller x2 = new XStreamMarshaller();
    public static org.kie.dmn.backend.marshalling.v1x.XStreamMarshaller x3 = new org.kie.dmn.backend.marshalling.v1x.XStreamMarshaller();
    public static Definitions unmarshal = x2.unmarshal(new StringReader(MODEL));
    public static Definitions unmarshal3 = x3.unmarshal(new StringReader(MODEL));
    public static StaxDriver staxDriver = new StaxDriver();
    public static XMLStreamReader xmlReader;
    public static CustomStaxReader customStaxReader;
    static {
        try {
            xmlReader = staxDriver.getInputFactory().createXMLStreamReader(new StringReader(MODEL));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        customStaxReader = new CustomStaxReader(new QNameMap(), xmlReader);
        System.out.println(customStaxReader.getNsContext().values());
        Resource modelResource = ResourceFactory.newReaderResource(new StringReader(MODEL));
        dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(Arrays.asList(modelResource)).getOrElseThrow(RuntimeException::new);
    }

    public static String asd(String xml) {
        return x3.unmarshal(new StringReader(xml)).getNamespace();
    }
    
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String jitdmn(JITDMNPayload payload) {
        String modelXML = payload.getModel();
        System.out.println(modelXML);
        Definitions unmarshal = DMNMarshallerFactory.newDefaultMarshaller().unmarshal(new StringReader(payload.getModel()));
        return unmarshal.getName();
    }
}