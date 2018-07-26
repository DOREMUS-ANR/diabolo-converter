package org.doremus.diaboloConverter.musResource;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.Time;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class E52_TimeSpan extends DoremusResource {
  public static final DateFormat ISODateFormat = new SimpleDateFormat("yyyy-MM-dd");
  private Literal start, end;

  private Precision quality;

  public Literal getStart() {
    return start;
  }


  public enum Precision {
    CERTAINTY("certain"),
    UNCERTAINTY("uncertain"),
    DECADE("precision at decade"),
    CENTURY("precision at century");

    private final String text;

    Precision(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }

  }

  public E52_TimeSpan(URI uri, Date start, Date end) {
    super(uri);

    String label = ISODateFormat.format(start).substring(0, 10);
    if (!end.equals(start)) label += "/" + ISODateFormat.format(end).substring(0, 10);
    this.start = model.createTypedLiteral(ISODateFormat.format(start), XSDDatatype.XSDdate);
    this.end = model.createTypedLiteral(ISODateFormat.format(end), XSDDatatype.XSDdate);

    this.resource = model.createResource(uri.toString())
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDF.type, Time.Interval)
      .addProperty(RDFS.label, label)
      .addProperty(Time.hasBeginning,
        model.createResource()
          .addProperty(RDF.type, Time.Instant)
          .addProperty(Time.inXSDDate, this.start))
      .addProperty(Time.hasEnd, model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, this.end));
  }

  public E52_TimeSpan(URI uri, Literal start, Literal end) {
    super(uri);
    this.start = start;
    this.end = end;


    boolean startUncertain = false;
    if (this.start == null) {
      this.start = this.end;
      startUncertain = true;
    }

    String label = this.start.getLexicalForm();
    if (end != null && !end.equals(this.start)) label += "/" + end.getLexicalForm();

    this.resource = model.createResource(uri.toString())
      .addProperty(RDF.type, CIDOC.E52_Time_Span)
      .addProperty(RDF.type, Time.Interval)
      .addProperty(RDFS.label, label);

    if (startUncertain)
      this.resource.addProperty(CIDOC.P79_beginning_is_qualified_by, Precision.UNCERTAINTY.toString());

    this.resource.addProperty(Time.hasBeginning,
      model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, this.start));

    if (this.end != null)
      this.resource.addProperty(Time.hasEnd, model.createResource()
        .addProperty(RDF.type, Time.Instant)
        .addProperty(Time.inXSDDate, this.end));
  }

  public void setQuality(Precision quality) {
    if (quality == null) return;
    this.quality = quality;

    this.resource.addProperty(CIDOC.P79_beginning_is_qualified_by, quality.toString())
      .addProperty(CIDOC.P80_end_is_qualified_by, quality.toString());
  }

}
