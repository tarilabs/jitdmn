package org.acme;

import java.util.Map;

public class JITDMNPayload {
    private String model;
    private Map<String, Object> context;
    
    public JITDMNPayload() {
    }

    public JITDMNPayload(String model, Map<String, Object> context) {
        this.model = model;
        this.context = context;
    }

    

    public String getModel() {
        return model;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

}
