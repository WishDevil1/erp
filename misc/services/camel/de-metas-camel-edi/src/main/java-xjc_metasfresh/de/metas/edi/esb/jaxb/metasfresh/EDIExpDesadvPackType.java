//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.11.22 at 01:55:45 PM EET 
//


package de.metas.edi.esb.jaxb.metasfresh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for EDI_Exp_Desadv_PackType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EDI_Exp_Desadv_PackType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GTIN_LU_PackingMaterial" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="IPA_SSCC18" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="M_HU_PackagingCode_LU_Text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="EDI_Exp_Desadv_Pack_Item" type="{}EDI_Exp_Desadv_Pack_ItemType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EDI_Exp_Desadv_PackType", propOrder = {
    "gtinluPackingMaterial",
    "ipasscc18",
    "mhuPackagingCodeLUText",
    "ediExpDesadvPackItem"
})
public class EDIExpDesadvPackType {

    @XmlElement(name = "GTIN_LU_PackingMaterial")
    protected String gtinluPackingMaterial;
    @XmlElement(name = "IPA_SSCC18")
    protected String ipasscc18;
    @XmlElement(name = "M_HU_PackagingCode_LU_Text")
    protected String mhuPackagingCodeLUText;
    @XmlElement(name = "EDI_Exp_Desadv_Pack_Item")
    protected List<EDIExpDesadvPackItemType> ediExpDesadvPackItem;

    /**
     * Gets the value of the gtinluPackingMaterial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGTINLUPackingMaterial() {
        return gtinluPackingMaterial;
    }

    /**
     * Sets the value of the gtinluPackingMaterial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGTINLUPackingMaterial(String value) {
        this.gtinluPackingMaterial = value;
    }

    /**
     * Gets the value of the ipasscc18 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIPASSCC18() {
        return ipasscc18;
    }

    /**
     * Sets the value of the ipasscc18 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIPASSCC18(String value) {
        this.ipasscc18 = value;
    }

    /**
     * Gets the value of the mhuPackagingCodeLUText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMHUPackagingCodeLUText() {
        return mhuPackagingCodeLUText;
    }

    /**
     * Sets the value of the mhuPackagingCodeLUText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMHUPackagingCodeLUText(String value) {
        this.mhuPackagingCodeLUText = value;
    }

    /**
     * Gets the value of the ediExpDesadvPackItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ediExpDesadvPackItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEDIExpDesadvPackItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EDIExpDesadvPackItemType }
     * 
     * 
     */
    public List<EDIExpDesadvPackItemType> getEDIExpDesadvPackItem() {
        if (ediExpDesadvPackItem == null) {
            ediExpDesadvPackItem = new ArrayList<EDIExpDesadvPackItemType>();
        }
        return this.ediExpDesadvPackItem;
    }

}
