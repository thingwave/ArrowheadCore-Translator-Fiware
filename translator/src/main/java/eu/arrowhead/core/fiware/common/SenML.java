/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.core.fiware.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Pablo Puñal Pereira <pablo.punal@thingwave.eu>
 */
public class SenML {
    private final GsonBuilder gsonBuilder = new GsonBuilder().disableHtmlEscaping();
    private final Gson gson = gsonBuilder.create();
    private final Gson pretyGson = gsonBuilder.setPrettyPrinting().create();
    private final ArrayList<SenMLElement> values;
    
    public SenML() {
        values = new ArrayList<>();
    }
    
    public SenML addElement(SenMLElement value) {
        values.add(value);
        return this;
    }
    
    public SenML fromJSON(String json) {
        Collection<SenMLElement> tmp = gson.fromJson(json, new TypeToken<Collection<SenMLElement>>(){}.getType());
        values.clear();
        tmp.forEach(senMLvalue -> { values.add(senMLvalue); });
        return this;
    }
    
    public SenMLElement getLastest() {
        // TODO: add suport by name
        if (values.isEmpty()) return new SenMLElement();
        
        double time = 0;
        Number bt = 0;
        String bn = null;
        int index = 0;
        for (int i = 0; i< values.size(); i++) {
            if (values.get(i).getTime() != null) {
                if (time > values.get(i).getTime().doubleValue()) {
                    time = values.get(i).getTime().doubleValue();
                    index = i;
                }
            }
            if (values.get(i).getBaseTime() != null) {
                bt = values.get(i).getBaseTime();
            }
            if (values.get(i).getBaseName() != null) {
                bn = values.get(i).getBaseName();
            }
        }
        
        values.get(index).setBaseTime(bt);
        values.get(index).setBaseName(bn);
        
        return values.get(index);
    }
    
    public String toJSON() {
        return gson.toJson(values);
    }
    
    public String toPrettyJSON() {
        return pretyGson.toJson(values);
    }
    
    // RFC 8428
    public static class SenMLElement {
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
        
        public SenMLElement(String bn, Number bt, String bu, Number bv, Number bs,
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
        
        public SenMLElement() {}
        
        public String getBaseName() {
            return bn;
        }
        
        public SenMLElement setBaseName(String bn) {
            this.bn = bn;
            return this;
        }
        
        public Number getBaseTime() {
            return bt;
        }
        
        public SenMLElement setBaseTime(Number bt) {
            this.bt = bt;
            return this;
        }
        
        public String getBaseUnit() {
            return bu;
        }
        
        public SenMLElement setBaseUnit(String bu) {
            this.bu = bu;
            return this;
        }
        
        public Number getBaseValue() {
            return bv;
        }
        
        public SenMLElement setBaseValue(Number bv) {
            this.bv = bv;
            return this;
        }
        
        public Number getBaseSum() {
            return bs;
        }
        
        public SenMLElement setBaseSum(Number bs) {
            this.bs = bs;
            return this;
        }
        
        public Number getBaseVersion() {
            return bver;
        }
        
        public SenMLElement setBaseVersion(Number bver) {
            this.bver = bver;
            return this;
        }
        
        public String getName() {
            return n;
        }
        
        public SenMLElement setName(String n) {
            this.n = n;
            return this;
        }
        
        public String getUnit() {
            return u;
        }
        
        public SenMLElement setUnit(String u) {
            this.u = u;
            return this;
        }
        
        public Number getValue() {
            return v;
        }
        
        public SenMLElement setValue(Number v) {
            this.v = v;
            return this;
        }
        
        public String getStringValue() {
            return vs;
        }
        
        public SenMLElement setStringValue(String vs) {
            this.vs = vs;
            return this;
        }
        
        public Boolean getBooleanValue() {
            return vb;
        }
        
        public SenMLElement setBooleanValue(Boolean vb) {
            this.vb = vb;
            return this;
        }
        
        public String getDataValue() {
            return vd;
        }
        
        public SenMLElement setDataValue(String vd) {
            this.vd = vd;
            return this;
        }
        
        public Number getSum() {
            return s;
        }
        
        public SenMLElement setSum(Number s) {
            this.s = s;
            return this;
        }
        
        public Number getTime() {
            return t;
        }
        
        public SenMLElement setTime(Number t) {
            this.t = t;
            return this;
        }
        
        public Number getUpdateTime() {
            return ut;
        }
        
        public SenMLElement setUpdateTime(Number ut) {
            this.ut = ut;
            return this;
        }
    }
}
