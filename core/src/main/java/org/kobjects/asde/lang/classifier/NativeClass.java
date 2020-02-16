package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.type.TypeImpl;

import java.util.Collection;
import java.util.TreeMap;

public class NativeClass extends TypeImpl implements Classifier {

  private final TreeMap<String, Property> propertyDescriptors = new TreeMap<>();
  private final CharSequence documentation;

  public NativeClass(String name, CharSequence documentation) {
    super(name, null);
    this.documentation = documentation;
  }

  /**
   * Properties are added separately to allow for circular references.
   */
  public void addProperties(Property... properties) {
    for (Property property : properties) {
      this.propertyDescriptors.put(property.getName(), property);
    }
  }

  public Property getPropertyDescriptor(String name) {
    return propertyDescriptors.get(name);
  }

  @Override
  public Collection<? extends Property> getPropertyDescriptors() {
    return propertyDescriptors.values();
  }

  public CharSequence getDocumentation() {
    return documentation;
  }
}
