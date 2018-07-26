package org.doremus.diaboloConverter;

import net.sf.junidecode.Junidecode;
import org.apache.http.client.utils.URIBuilder;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.UUID;

public class ConstructURI {
  private static URIBuilder builder = new URIBuilder().setScheme("http").setHost("data.doremus.org");

  public static URI build(String db, String className, String identifier) throws URISyntaxException {
    String seed = db + identifier + className;
    return builder.setPath("/" + getCollectionName(className) + "/" + generateUUID(seed)).build();
  }

  public static URI build(String className, String firstName, String lastName, String birthDate) throws URISyntaxException {
    String seed = norm(firstName + lastName + birthDate);
    return builder.setPath("/" + getCollectionName(className) + "/" + generateUUID(seed)).build();
  }

  private static String generateUUID(String seed) {
    // source: https://gist.github.com/giusepperizzo/630d32cc473069497ac1
    try {
      String hash = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(seed.getBytes("UTF-8")));
      UUID uuid = UUID.nameUUIDFromBytes(hash.getBytes());
      return uuid.toString();
    } catch (Exception e) {
      System.err.println("[ConstructURI.java]" + e.getLocalizedMessage());
      return "";
    }
  }

  private static String norm(String input) {
    // remove punctuation, ascii transliteration
    return Junidecode.unidecode(input.replaceAll("[.,\\/#!$%\\^&\\*;:{}=\\-_`~()]", " "));
  }


  private static String getCollectionName(String className) {
    switch (className) {
      case "F22_SelfContainedExpression":
      case "F25_PerformancePlan":
      case "M43_PerformedExpression":
        return "expression";
      case "F28_ExpressionCreation":
      case "F30_PublicationEvent":
      case "M45_DescriptiveExpressionAssignment":
      case "F42_RepresentativeExpressionAssignment":
      case "F29_RecordingEvent":
        return "event";
      case "F14_IndividualWork":
      case "F15_ComplexWork":
      case "F19_PublicationWork":
      case "M44_PerformedWork":
      case "F20_PerformanceWork":
      case "F21_RecordingWork":
        return "work";
      case "F24_PublicationExpression":
        return "publication";
      case "F31_Performance":
      case "M42_PerformedExpressionCreation":
      case "M28_IndividualPerformance":
        return "performance";
      case "F26_Recording":
        return "recording";
      case "E21_Person":
      case "F11_Corporate_Body":
        return "artist";
      case "E4_Period":
        return "period";
      case "E53_Place":
        return "place";
      case "theme":
        return "theme";
      case "prov":
        return "activity";
      case "M31_ActorFunction":
      case "M32_ActorResponsibility":
        return "function";
      case "M14_MediumOfPerformance":
        return "mop";
      case "M29_Editing":
        return "editing";
      case "M46_Set_of_Tracks":
      case "F4_ManifestationSingleton":
        return "manifestation";
      case "M24_Track":
        return "track";
      default:
        throw new RuntimeException("[ConstructURI.java] Class not assigned to a collection: " + className);
    }
  }
}
