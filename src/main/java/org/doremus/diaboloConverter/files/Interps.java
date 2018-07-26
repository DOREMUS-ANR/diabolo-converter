package org.doremus.diaboloConverter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class Interps {
  @XmlElement(name = "DATA_RECORD")
  private List<Interp> interps;


  public List<Interp> authorsOf(String workId) {
    return interps.stream()
      .filter(x -> workId.equals(x.getWorkId()))
      .filter(Interp::isAuthor)
      .filter(x -> Function.isInList(x.getFunct(), true))
      .collect(Collectors.toList());
  }


  public static Interps fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Interps.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (Interps) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

}
