package org.kobjects.typesystem;

import java.util.TreeMap;

public class InstanceTypeImpl extends TypeImpl implements InstanceType {

  private final TreeMap<String, PropertyDescriptor> propertyDescriptors = new TreeMap<>();

  public InstanceTypeImpl(String name) {
    super(name, null);
  }

  /**
   * Properties are added separately to allow for circular references.
   */
  public void addProperties(PropertyDescriptor... properties) {
    for (PropertyDescriptor property : properties) {
      this.propertyDescriptors.put(property.name(), property);
    }
  }

  public PropertyDescriptor getPropertyDescriptor(String name) {
        return propertyDescriptors.get(name);
    }
}
