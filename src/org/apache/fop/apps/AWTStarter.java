/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;
/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 * Modified to use streaming API by Mark Lillywhite, mark-fop@inomial.com
 */
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.viewer.*;
import org.apache.fop.render.awt.*;


import javax.swing.UIManager;
import java.awt.*;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



// Java
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;



/**
 * initialize AWT previewer
 */

public class AWTStarter extends CommandLineStarter {

    PreviewDialog frame;
    AWTRenderer renderer;
    public static String TRANSLATION_PATH =
        "/org/apache/fop/viewer/resources/";

    private Translator resource;

    public AWTStarter(CommandLineOptions commandLineOptions)
    throws FOPException {
        super(commandLineOptions);
        init();
    }

    private void init() {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String language = commandLineOptions.getLanguage();

        if (language == null)
            language = System.getProperty("user.language");

        resource = getResourceBundle(TRANSLATION_PATH + "resources."
                                     + language);

        UserMessage.setTranslator(getResourceBundle(TRANSLATION_PATH
                                  + "messages."
                                  + language));

        resource.setMissingEmphasized(false);
        renderer = new AWTRenderer(resource);
        frame = createPreviewDialog(renderer, resource);
        renderer.setProgressListener(frame);
        renderer.setComponent(frame);
        MessageHandler.setOutputMethod(MessageHandler.EVENT);
        MessageHandler.addListener(frame);
    }


    public void run() throws FOPException {
        Driver driver = new Driver();
        if (errorDump) {
            driver.setErrorDump(true);
        }

        // init parser
        frame.progress(resource.getString("Init parser") + " ...");
        XMLReader parser = inputHandler.getParser();

        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        setParserFeatures(parser);

        try {
            driver.setRenderer(renderer);

            // build FO tree: time
            frame.progress(resource.getString("Build FO tree") + " ...");
            driver.render(parser, inputHandler.getInputSource());

            frame.progress(resource.getString("Show"));
            frame.showPage();

        } catch (Exception e) {
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }

    }

    protected PreviewDialog createPreviewDialog(AWTRenderer renderer,
            Translator res) {
        PreviewDialog frame = new PreviewDialog(renderer, res);
        frame.validate();

        // center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
        return frame;
    }



    private SecureResourceBundle getResourceBundle(String path) {
        InputStream in = null;

        try {
            URL url = getClass().getResource(path);
            in = url.openStream();
        } catch (Exception ex) {
            MessageHandler.logln("Can't find URL to: <" + path + "> "
                                 + ex.getMessage());
        }
        return new SecureResourceBundle(in);
    }

}



