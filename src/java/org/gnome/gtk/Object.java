/*
 * Object.java
 *
 * Copyright (c) 2006 Operational Dynamics Consulting Pty Ltd and Others
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" plus the "Classpath Exception" (you may link to this code as a
 * library into other programs provided you don't make a derivation of it).
 * See the LICENCE file for the terms governing usage and redistribution.
 */
package org.gnome.gtk;

import org.gnome.gdk.Pixbuf;

/**
 * <i><b>This is the wrapper around <code>GtkObject</code>!</b></i>
 */
public abstract class Object extends org.gnome.glib.Object
{
    protected Object(long pointer) {
        super(pointer);
    }

    protected void setPropertyPixbuf(String name, Pixbuf pixbuf) {
        setProperty(name, pixbuf);
    }
}
