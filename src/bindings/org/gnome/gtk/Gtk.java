/*
 * Gtk.java
 *
 * Copyright (c) 2006-2007 Operational Dynamics Consulting Pty Ltd
 * 
 * The code in this file, and the library it is a part of, are made available
 * to you by the authors under the terms of the "GNU General Public Licence,
 * version 2" plus the "Classpath Exception" (you may link to this code as a
 * library into other programs provided you don't make a derivation of it).
 * See the LICENCE file for the terms governing usage and redistribution.
 */
package org.gnome.gtk;

import org.gnome.gdk.Gdk;
import org.gnome.glib.Glib;

/**
 * The GTK widget toolkit initialization and main loop entry point. A typical
 * program written with java-gnome will boil down to this:
 * 
 * <pre>
 * public class ComeOnBabyLightMyFire
 *   
 *     public static void main(String[] args) {
 *         Gtk.init(args);
 *           
 *         // build user interface
 *           
 *         Gtk.main();
 *     }
 * }
 * </pre>
 * 
 * There. Now you know everything you need to know. <code>:)</code> In due
 * course we will write some tutorials to help you along.
 * 
 * @author Andrew Cowie
 * @since 4.0.0
 */
/*
 * Extremely atypically, this class contains native declarations because a)
 * There's nothing left if you strip off the Gtk prefix from Gtk, b) no reason
 * to use a static class GtkMain or whatever as none of these methods need
 * access to Plumbing.
 */
public final class Gtk extends Glib
{
    static {
        System.loadLibrary("gtkjni-" + Version.APIVERSION);
    }

    /**
     * No instantiation. Static methods only!
     */
    private Gtk() {}

    /**
     * A guard against someone calling init() twice
     */
    private static boolean initialized = false;

    /**
     * Initialize the GTK libraries. <b>This must be called before any other
     * org.gnome.* classes are used.</b>
     * 
     * @param args
     *            The command line arguments array. This is passed to the
     *            underlying library to allowing user (or window manager) to
     *            alter GTK's behaviour.
     * @since 4.0.0
     */
    public static void init(String[] args) {
        if (initialized) {
            throw new IllegalStateException("Gtk already initialized");
        }

        /*
         * Notify org.gnome.glib.Glib that we don't need it to do anything
         */
        Glib.skipInit();

        /*
         * Initialize GTK and along with it GLib, GObject, etc.
         */
        gtk_init(Gdk.lock, args);

        initialized = true;
    }

    /*
     * This is one of the rarer cases where the arguments we pass to the JNI
     * side have little relation to the signature of the actual target
     * function. In this case, the first argument is a reference to the GDK
     * lock used to permit multithreaded access to the GTK library.
     */
    private static native final void gtk_init(java.lang.Object lock, String[] args);

    /**
     * This method blocks, ie, it does not return until the GTK main loop is
     * terminated.
     * <p>
     * You can nest calls to <code>Gtk.main()</code>! If you do, then
     * calling {@link #mainQuit() mainQuit()} will make the innermost
     * invocation of the main loop return. (This is how modal Dialog boxes run
     * and block the rest of the application while still accepting events
     * themselves)
     * 
     * @since 4.0.0
     */
    /*
     * Note that although this code is marked as being within the Gdk$Lock,
     * there is, in effect, a wait() within this call: as the GTK main loop
     * cycles it releases the lock [via gdk_threads_leave()?] and then
     * reestablishes [via gdk_threads_enter()? No matter - the custom lock
     * functions get hit]. The effect is that the monitor on Gdk.lock is
     * frequently relinquished, which is the behaviour that is expected if a
     * piece of Java code object executes wait() within a monitor block. Which
     * is exactly what we need! The only tiny hiccup is that the thread dump
     * [via Ctrl+\] and debugger don't seem to quite realize that this thread
     * no longer owns the lock.
     */
    public static void main() {
        synchronized (Gdk.lock) {
            gtk_main();
        }
    }

    private static native final void gtk_main();

    /**
     * Exit the main loop. Since main loops can be nested, this does not
     * necessarily imply application termination, but if you have a typical
     * GTK program with a single call to <code>Gtk.main()</code> at the end
     * of your Java <code>main()</code> function, then calling
     * <code>Gtk.mainQuit()</code> in a signal handler somewhere will return
     * the program flow to <code>main()</code> on your way exiting.
     * 
     * @since 4.0.0
     */
    public static void mainQuit() {
        gtk_main_quit();
    }

    private static native final void gtk_main_quit();

    /**
     * Are there any events pending for the main loop to process?
     * 
     * <p>
     * <b>This is not for general use! Do not expose this and do not encourage
     * anyone to use this to hack into the main loop.</b>
     * 
     * <p>
     * In a test case, this could be used as follows; see
     * <code>TestCaseGtk.cycleMainLoop()</codde> in the <code>tests/</code> tree for
     * details:
     * 
     * <pre>
     * while (Gtk.eventsPending()) {
     *     Gtk.mainIterationDo(false);
     * }
     * </pre>
     */
    static final boolean eventsPending() {
        synchronized (Gdk.lock) {
            return gtk_events_pending();
        }
    }

    private static native final boolean gtk_events_pending();

    /**
     * Run a single iteration of the main loop.
     * 
     * <p>
     * Not public! This is for internal use only, notably by test cases.
     * 
     * @param block
     *            Whether to block or not. If <code>true</code>, this
     *            method will block until an event is processed.
     * @return Will result in <code>true</code> if
     *         <code>Gtk.mainQuit()</code> (aka <code>gtk_main_quit()</code>)
     *         has been called on the innermost active main loop.
     *         <code>true</code> will also be returned if there <i>is</i>
     *         no main loop running.
     */
    static final boolean mainIterationDo(boolean block) {
        synchronized (Gdk.lock) {
            return gtk_main_iteration_do(block);
        }
    }

    private static native final boolean gtk_main_iteration_do(boolean blocking);
}
