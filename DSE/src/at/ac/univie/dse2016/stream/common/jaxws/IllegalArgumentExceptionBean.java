
package at.ac.univie.dse2016.stream.common.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class was generated by Apache CXF 3.1.8
 * Tue Nov 22 19:58:05 CET 2016
 * Generated source version: 3.1.8
 */

@XmlRootElement(name = "IllegalArgumentException", namespace = "http://lang.java/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IllegalArgumentException", namespace = "http://lang.java/")

public class IllegalArgumentExceptionBean {

    private java.lang.String message;

    public java.lang.String getMessage() {
        return this.message;
    }

    public void setMessage(java.lang.String newMessage)  {
        this.message = newMessage;
    }

}

