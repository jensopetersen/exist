/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2012 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.launcher;

import org.apache.log4j.Logger;
import org.exist.jetty.JettyStart;
import org.exist.storage.BrokerPool;
import org.exist.util.ConfigurationHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;

/**
 * A launcher for the eXist-db server integrated with the desktop.
 * Shows a splash screen during startup and registers a tray icon
 * in the system bar.
 *
 * @author Wolfgang Meier
 */
public class Launcher {

    private final static Logger LOG = Logger.getLogger(Launcher.class);

    private MenuItem stopItem;
    private MenuItem startItem;

    public static void main(final String[] args) {
        String os = System.getProperty("os.name", "");
        // Switch to native look and feel except for Linux (ugly)
        if (!os.equals("Linux")) {
            String nativeLF = UIManager.getSystemLookAndFeelClassName();
            try {
                UIManager.setLookAndFeel(nativeLF);
            } catch (Exception e) {
                // can be safely ignored
            }
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Launcher(args);
            }
        });
    }

    private TrayIcon trayIcon = null;
    private SplashScreen splash;
    private JettyStart jetty;

    public Launcher(final String[] args) {
        if (!SystemTray.isSupported()) {
            showMessageAndExit("Not supported", "Running eXist-db via the launcher does not appear to be supported on your platform. " +
                    "Please run it using startup.sh/startup.bat.", false);
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        final String home = getJettyHome();
        captureConsole();

        Dimension iconDim = tray.getTrayIconSize();
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource("icon32.png"));
        } catch (IOException e) {
            showMessageAndExit("Launcher failed", "Failed to read system tray icon.", false);
        }
        trayIcon = new TrayIcon(image.getScaledInstance(iconDim.width, iconDim.height, Image.SCALE_SMOOTH), "eXist-db Launcher");

        final JDialog hiddenFrame = new JDialog();
        hiddenFrame.setUndecorated(true);

        final PopupMenu popup = createMenu(home, tray);
        trayIcon.setPopupMenu(popup);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    hiddenFrame.add(popup);
                    popup.show(hiddenFrame, mouseEvent.getXOnScreen(), mouseEvent.getYOnScreen());
                }
            }
        });
        try {
            hiddenFrame.setResizable(false);
            hiddenFrame.setVisible(true);
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
            showMessageAndExit("Not supported", "Running eXist-db via the launcher is not supported on your platform. " +
                    "Please run it using bin/startup.sh or bin\\startup.bat.", false);
            return;
        }
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                trayIcon.displayMessage(null, "Right click for menu", TrayIcon.MessageType.INFO);
            }
        });

        splash = new SplashScreen(this);
        splash.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            jetty = new JettyStart();
                            jetty.addObserver(splash);
                            jetty.run(new String[]{home}, splash);
                        } catch (Exception e) {
                            showMessageAndExit("Error Occurred", "An error occurred during startup. Please check the logs.", true);
                        }
                    }
                }.start();
            }
        });
    }

    private PopupMenu createMenu(final String home, final SystemTray tray) {
        PopupMenu popup = new PopupMenu();
        startItem = new MenuItem("Start server");
        startItem.setEnabled(false);
        popup.add(startItem);
        startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
            if (jetty.isStarted()) {
                trayIcon.displayMessage(null, "Server already started", TrayIcon.MessageType.WARNING);
            } else {
                jetty.run(new String[]{home}, null);
                if (jetty.isStarted()) {
                    stopItem.setEnabled(true);
                    startItem.setEnabled(false);
                    trayIcon.setToolTip("eXist-db server running on port " + jetty.getPrimaryPort());
                }
            }
            }
        });

        stopItem = new MenuItem("Stop server");
        popup.add(stopItem);
        stopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jetty.shutdown();
                stopItem.setEnabled(false);
                startItem.setEnabled(true);
                trayIcon.setToolTip("eXist-db stopped");
            }
        });

        MenuItem item;

        if (Desktop.isDesktopSupported()) {
            popup.addSeparator();
            final Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                item = new MenuItem("Open dashboard");
                popup.add(item);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            URI url = new URI("http://localhost:" + jetty.getPrimaryPort() + "/exist/apps/dashboard/");
                            desktop.browse(url);
                        } catch (URISyntaxException e) {
                            trayIcon.displayMessage(null, "Failed to open URL", TrayIcon.MessageType.ERROR);
                        } catch (IOException e) {
                            trayIcon.displayMessage(null, "Failed to open URL", TrayIcon.MessageType.ERROR);
                        }
                    }
                });
                item = new MenuItem("Open eXide");
                popup.add(item);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            URI url = new URI("http://localhost:" + jetty.getPrimaryPort() + "/exist/apps/eXide/");
                            desktop.browse(url);
                        } catch (URISyntaxException e) {
                            trayIcon.displayMessage(null, "Failed to open URL", TrayIcon.MessageType.ERROR);
                        } catch (IOException e) {
                            trayIcon.displayMessage(null, "Failed to open URL", TrayIcon.MessageType.ERROR);
                        }
                    }
                });
                item = new MenuItem("Open Java Admin Client");
                popup.add(item);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        LauncherWrapper wrapper = new LauncherWrapper("client");
                        wrapper.launch();
                    }
                });
            }
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                popup.addSeparator();
                item = new MenuItem("Open exist.log");
                popup.add(item);
                item.addActionListener(new LogActionListener());
            }

            popup.addSeparator();
            item = new MenuItem("Quit (and stop server)");
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                jetty.shutdown();
                tray.remove(trayIcon);
                System.exit(0);
                }
            });
        }
        return popup;
    }

    protected void signalStarted() {
        trayIcon.setToolTip("eXist-db server running on port " + jetty.getPrimaryPort());
        startItem.setEnabled(false);
        stopItem.setEnabled(true);
    }

    protected void signalShutdown() {
        trayIcon.setToolTip("eXist-db server stopped");
        startItem.setEnabled(true);
        stopItem.setEnabled(false);
    }

    private String getJettyHome() {
        String jettyProperty = System.getProperty("jetty.home");
        if(jettyProperty==null) {
            File home = ConfigurationHelper.getExistHome();
            File jettyHome = new File(new File(home, "tools"), "jetty");
            jettyProperty = jettyHome.getAbsolutePath();
            System.setProperty("jetty.home", jettyProperty);
        }
        File standaloneFile = new File(new File(jettyProperty, "etc"), "jetty.xml");
        return standaloneFile.getAbsolutePath();
    }

    protected void showMessageAndExit(String title, String message, boolean logs) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        if (logs) {
            JButton displayLogs = new JButton("View Log");
            displayLogs.addActionListener(new LogActionListener());
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(displayLogs, BorderLayout.SOUTH);
        }
        JOptionPane.showMessageDialog(splash, panel, title, JOptionPane.WARNING_MESSAGE);
        System.exit(1);
    }

    /**
     * Ensure that stdout and stderr messages are also printed
     * to the logs.
     */
    private void captureConsole() {
        System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realStream) {
        return new PrintStream(realStream) {
            @Override
            public void print(String s) {
                realStream.print(s);
                LOG.info(s);
            }
        };
    }

    private class LogActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!Desktop.isDesktopSupported())
                return;
            Desktop desktop = Desktop.getDesktop();
            File home = ConfigurationHelper.getExistHome();
            File logFile = new File(home, "webapp/WEB-INF/logs/exist.log");
            if (!logFile.canRead()) {
                trayIcon.displayMessage(null, "Log file not found", TrayIcon.MessageType.ERROR);
            } else {
                try {
                    desktop.open(logFile);
                } catch (IOException e) {
                    trayIcon.displayMessage(null, "Failed to open log file", TrayIcon.MessageType.ERROR);
                }
            }
        }
    }
}
