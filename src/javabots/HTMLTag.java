/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javabots;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HTMLTag {
    /*
     * The attributes
     */

    private Map<String, String> attributes = new HashMap<String, String>();

    /**
     * The tag name.
     */
    private String name = "";
    /*
     * Is this both a beginning and ending tag.
     */

    private boolean ending;

    public void clear() {
        this.attributes.clear();
        this.name = "";
        this.ending = false;
    }


    public Map<String, String> getAttributes() {
        return attributes;
    }


    /**
     * Get the value of the specified attribute.
     *
     * @param name
     * The name of an attribute.
     * @return The value of the specified attribute.
     */
    public String getAttributeValue(String name) {
        return this.attributes.get(name.toLowerCase());
    }


    /**
     * Get the tag name.
     */
    public String getName() {
        return this.name;
    }


    /**
     * @return the ending
     */
    public boolean isEnding() {
        return this.ending;
    }


    /**
     * Set a HTML attribute.
     *
     * @param name
     * The name of the attribute.
     * @param value
     * The value of the attribute.
     */
    public void setAttribute(String name, String value) {
        this.attributes.put(name.toLowerCase(), value);
    }


    /**
     * @param ending
     * The ending to set.
     */
    public void setEnding(boolean ending) {
        this.ending = ending;
    }


    /**
     * Set the tag name.
     */
    public void setName(String s) {
        this.name = s;
    }


    /**
     * Convert this tag back into string form, with the
     * beginning < and ending >.
     *
     * @param id
     * A zero based index that specifies the
     * attribute to retrieve.
     * @return The Attribute object that was found.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("<");
        buffer.append(this.name);
        Set<String> set = this.attributes.keySet();
        for (String key : set) {
            String value = this.attributes.get(key);
            buffer.append(' ');
            if (value == null) {
                buffer.append("\"");
                buffer.append(key);
                buffer.append("\"");
            } else {
                buffer.append(key);
                buffer.append("=\"");
                buffer.append(value);
                buffer.append("\"");
            }
        }
        if (this.ending) {
            buffer.append('/');
        }
        buffer.append(">");
        return buffer.toString();
    }


}
