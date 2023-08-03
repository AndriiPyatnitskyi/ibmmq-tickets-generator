package com.ibm.mq.events;
import java.io.StringWriter;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@XmlRootElement
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Advert {
    private Integer eventID;
    private String title;
    private Integer capacity;
    private String time;
    private String date;
    private String location;

    public String toXML() throws JAXBException {
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(this.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(this, writer);
        return writer.toString();
    }
}