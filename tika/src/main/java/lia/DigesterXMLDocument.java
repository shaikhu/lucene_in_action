package lia;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.xml.sax.SAXException;

public class DigesterXMLDocument {
  private Digester dig;

  private static Document doc;

  public DigesterXMLDocument() {
    dig = new Digester();
    dig.setValidating(false);
    dig.addObjectCreate("address-book", DigesterXMLDocument.class);
    dig.addObjectCreate("address-book/contact", Contact.class);
    dig.addSetProperties("address-book/contact", "type", "type");
    dig.addCallMethod("address-book/contact/name", "setName", 0);
    dig.addCallMethod("address-book/contact/address", "setAddress", 0);
    dig.addCallMethod("address-book/contact/city", "setCity", 0);
    dig.addCallMethod("address-book/contact/province", "setProvince", 0);
    dig.addCallMethod("address-book/contact/postalcode", "setPostalcode", 0);
    dig.addCallMethod("address-book/contact/country", "setCountry", 0);
    dig.addCallMethod("address-book/contact/telephone","setTelephone", 0);
    dig.addSetNext("address-book/contact", "populateDocument");
  }

  public synchronized Document getDocument(InputStream is) throws DocumentHandlerException {
    try {
      dig.parse(is);
    }
    catch (IOException | SAXException e) {
      throw new DocumentHandlerException("Cannot parse XML document", e);
    }
    return doc;
  }

  public void populateDocument(Contact contact) {
    doc = new Document();
    doc.add(new StringField("type", contact.getType(), Store.YES));
    doc.add(new StringField("name", contact.getName(), Store.YES));
    doc.add(new StringField("address", contact.getAddress(), Store.YES));
    doc.add(new StringField("city", contact.getCity(), Store.YES));
    doc.add(new StringField("province", contact.getProvince(), Store.YES));
    doc.add(new StringField("postalcode", contact.getPostalcode(), Store.YES));
    doc.add(new StringField("country", contact.getCountry(), Store.YES));
    doc.add(new StringField("telephone", contact.getTelephone(), Store.YES));
  }

  public static class Contact {
    private String type;
    private String name;
    private String address;
    private String city;
    private String province;
    private String postalcode;
    private String country;
    private String telephone;

    public void setType(String newType) {
      type = newType;
    }

    public String getType() {
      return type;
    }

    public void setName(String newName) {
      name = newName;
    }

    public String getName() {
      return name;
    }

    public void setAddress(String newAddress) {
      address = newAddress;
    }

    public String getAddress() {
      return address;
    }

    public void setCity(String newCity) {
      city = newCity;
    }

    public String getCity() {
      return city;
    }

    public void setProvince(String newProvince) {
      province = newProvince;
    }

    public String getProvince() {
      return province;
    }

    public void setPostalcode(String newPostalcode) {
      postalcode = newPostalcode;
    }

    public String getPostalcode() {
      return postalcode;
    }

    public void setCountry(String newCountry) {
      country = newCountry;
    }

    public String getCountry() {
      return country;
    }

    public void setTelephone(String newTelephone) {
      telephone = newTelephone;
    }

    public String getTelephone() {
      return telephone;
    }
  }

  public static void main(String... args) throws Exception {
    DigesterXMLDocument handler = new DigesterXMLDocument();
    Document doc = handler.getDocument(new FileInputStream(args[0]));
    System.out.println(doc);
  }
}
