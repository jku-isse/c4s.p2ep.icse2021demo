//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.0-M4 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.13 at 12:25:49 PM CEST 
//


package https.developers_google_com.blockly.xml;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * &lt;p&gt;Java class for anonymous complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;attribute name="text" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&amp;gt;
 *       &amp;lt;attribute name="callbackKey" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "button")
public class Button {

    @XmlAttribute(name = "text", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String text;
    @XmlAttribute(name = "callbackKey", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String callbackKey;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Gets the value of the callbackKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallbackKey() {
        return callbackKey;
    }

    /**
     * Sets the value of the callbackKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallbackKey(String value) {
        this.callbackKey = value;
    }

}