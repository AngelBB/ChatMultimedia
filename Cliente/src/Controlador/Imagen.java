/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;
import java.awt.Graphics;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
/**
 *
 * @author Angel
 */
public class Imagen extends javax.swing.JPanel {
    int x, y;
    URL url;
    
    public Imagen(JPanel jPanel1,URL url) {
        this.x = jPanel1.getWidth();
        this.y = jPanel1.getHeight();
        this.setSize(x, y);
        this.url=url;
    }
    
    
    
    
    
    @Override
    public void paint(Graphics g) {
        ImageIcon Img = new ImageIcon(this.url);
        g.drawImage(Img.getImage(), 0, 0, x, y, null);
    }    
}
