/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class SenML {
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final ArrayList<SenMLValue> values;
    
    public SenML() {
        values = new ArrayList<>();
    }
    
    public SenML addValue(SenMLValue value) {
        values.add(value);
        return this;
    }
    
    public String toJSON() {
        return gson.toJson(values);
    }
    
    public String toPrettyJSON() {
        return pretyGson.toJson(values);
    }
    
    // RFC 8428
    public static class SenMLValue {
        private String bn;  // Base Name
        private Number bt;  // Base Time
        private String bu;  // Base Unit
        private Number bv;  // Base Value
        private Number bs;  // Base Sum
        private Number bver;// Base Version
        private String n;   // Name
        private String u;   // Unit
        private Number v;   // Value
        private String vs;  // String Value
        private Boolean vb; // Boolean Value
        private String vd;  // Data Value
        private Number s;   // Sum
        private Number t;   // Time
        private Number ut;  // Update Time
        
        public SenMLValue(String bn, Number bt, String bu, Number bv, Number bs,
                Number bver, String n, String u, Number v, String vs, Boolean vb,
                String vd, Number s, Number t, Number ut) {
            this.bn = bn;
            this.bt = bt;
            this.bu = bu;
            this.bv = bv;
            this.bs = bs;
            this.bver = bver;
            this.n = n;
            this.u = u;
            this.v = v;
            this.vs = vs;
            this.vb = vb;
            this.vd = vd;
            this.s = s;
            this.t = t;
            this.ut = ut;
        }
        
        public SenMLValue() {}
        
        public String getBaseName() {
            return bn;
        }
        
        public SenMLValue setBaseName(String bn) {
            this.bn = bn;
            return this;
        }
        
        public Number getBaseTime() {
            return bt;
        }
        
        public SenMLValue setBaseTime(Number bt) {
            this.bt = bt;
            return this;
        }
        
        public String getBaseUnit() {
            return bu;
        }
        
        public SenMLValue setBaseUnit(String bu) {
            this.bu = bu;
            return this;
        }
        
        public Number getBaseValue() {
            return bv;
        }
        
        public SenMLValue setBaseValue(Number bv) {
            this.bv = bv;
            return this;
        }
        
        public Number getBaseSum() {
            return bs;
        }
        
        public SenMLValue setBaseSum(Number bs) {
            this.bs = bs;
            return this;
        }
        
        public Number getBaseVersion() {
            return bver;
        }
        
        public SenMLValue setBaseVersion(Number bver) {
            this.bver = bver;
            return this;
        }
        
        public String getName() {
            return n;
        }
        
        public SenMLValue setName(String n) {
            this.n = n;
            return this;
        }
        
        public String getUnit() {
            return u;
        }
        
        public SenMLValue setUnit(String u) {
            this.u = u;
            return this;
        }
        
        public Number getValue() {
            return v;
        }
        
        public SenMLValue setValue(Number v) {
            this.v = v;
            return this;
        }
        
        public String getStringValue() {
            return vs;
        }
        
        public SenMLValue setStringValue(String vs) {
            this.vs = vs;
            return this;
        }
        
        public Boolean getBooleanValue() {
            return vb;
        }
        
        public SenMLValue setBooleanValue(Boolean vb) {
            this.vb = vb;
            return this;
        }
        
        public String getDataValue() {
            return vd;
        }
        
        public SenMLValue setDataValue(String vd) {
            this.vd = vd;
            return this;
        }
        
        public Number getSum() {
            return s;
        }
        
        public SenMLValue setSum(Number s) {
            this.s = s;
            return this;
        }
        
        public Number getTime() {
            return t;
        }
        
        public SenMLValue setTime(Number t) {
            this.t = t;
            return this;
        }
        
        public Number getUpdateTime() {
            return ut;
        }
        
        public SenMLValue setUpdateTime(Number ut) {
            this.ut = ut;
            return this;
        }
    }
}
