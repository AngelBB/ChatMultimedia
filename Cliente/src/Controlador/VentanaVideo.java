/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;
import java.awt.BorderLayout;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Angel
 */
public class VentanaVideo extends JFrame{
    Player player;
    Component video;
    Component controles;
    URL url;
    
    //Titulo de video y ruta del archivo
    public VentanaVideo(String titulo,String pathArchivo){
        setTitle(titulo);
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        try {
            this.url=new URL(pathArchivo);
        } catch (MalformedURLException ex) {
            System.err.println(ex.getMessage());
        }
        init();
    }
 
    private void init() {
            //panel principal
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            
            try {
                player = Manager.createRealizedPlayer(new MediaLocator(this.url));
                video = player.getVisualComponent();
                video.setSize(800,500);
                video.setVisible(true);
                if(video != null)
                    panel.add("Center",video);
                
                controles = player.getControlPanelComponent();
                controles.setSize(800,100);
                controles.setVisible(true);
                if(controles != null)
                    panel.add("South",controles);
                
                add(panel);
                player.start();
                panel.updateUI();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
    }
}



    
    
    
    

