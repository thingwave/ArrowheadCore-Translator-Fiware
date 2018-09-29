/*
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.systemregistry.model;

import java.util.ArrayList;

public class Elements<T> {

  private ArrayList<T> elements;

  public Elements() {
    elements = new ArrayList<T>();
  }

  public void addElement(T identity) throws Exception {
    this.elements.add(identity);
  }

  public void removeElement(T identity) throws Exception {
    this.elements.remove(identity);
  }

  public ArrayList<T> getElements() {
    return this.elements;
  }

  public String toString() {
    String r = "";

    for (T identity : elements) {
      r = r + identity.toString();
    }

    return r;
  }
}
