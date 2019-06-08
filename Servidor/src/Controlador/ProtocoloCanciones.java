/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.ConectarBD;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Angel
 */


class ProtocoloCanciones {
private final ConectarBD cbd=new ConectarBD();
    
public ProtocoloCanciones(){
    cbd.iniciarConexion();//iniciamos la conexion en el modelo
}


//Songlist
public String comprobarDestinatarioSonglist(String cadena){
String ipDestino=null;
    
    //MSG:mensaje#FROM#login_sender#TO#login_receiver
    String [] aux=cadena.split("#");//pos 3 destinatario - pos 2 el remitente 
    String [] msg=aux[0].split(":");//pos 1 mensaje
     
    //cbd.iniciarConexion();
    ipDestino=cbd.IPPersonal(aux[3]);                  
      
    return ipDestino;

}
public boolean cadenaSonglist(String cadena){
    Pattern patron = Pattern.compile("^SONGLIST:.+");
    Matcher mat = patron.matcher(cadena);
        return  mat.matches();
}

//List
public String comprobarDestinatarioList(String cadena){
String ipDestino=null;
    
    //MSG:mensaje#FROM#login_sender#TO#login_receiver
    String [] aux=cadena.split("#");//pos 3 destinatario - pos 2 el remitente 
    String [] msg=aux[0].split(":");//pos 1 mensaje
     
    //cbd.iniciarConexion();
    ipDestino=cbd.IPPersonal(aux[3]);                  
      
    return ipDestino;


}
public boolean cadenaList(String cadena){
Pattern patron = Pattern.compile("^LIST:.+");
    Matcher mat = patron.matcher(cadena);
        return  mat.matches();
} 

//Play
public String comprobarPlayCancion(String cadena){
String ipDestino=null;
       
    String [] aux=cadena.split("#");//
    String [] msg=aux[0].split(":");//pos 1 mensaje
     
    //cbd.iniciarConexion();
    ipDestino=cbd.IPPersonal(aux[2]);                  
      
    return ipDestino;
} 


public boolean cadenaPlay(String cadena){
Pattern patron = Pattern.compile("^PLAY:.+");
    Matcher mat = patron.matcher(cadena);
        return  mat.matches();
}

//PlayData
public String reenviarPlayData(String cadena){
String ipDestino=null;
       //PLAYDATA:login_destino#datos_binarios#UsuarioOrigen 
    String [] aux=cadena.split("#");
    String [] loginDestino=aux[0].split(":");
     
    //cbd.iniciarConexion();
    ipDestino=cbd.IPPersonal(loginDestino[1]);                  
      
    return ipDestino;
}
public boolean cadenaPlayData(String cadena){
Pattern patron = Pattern.compile("^PLAYDATA:.+");
    Matcher mat = patron.matcher(cadena);
        return  mat.matches();
}

//PlayInfo
public String enviarPlayInfo(String cadena){
String ipDestino=null;
        
    String [] aux=cadena.split("#");//pos 3 destinatario - pos 2 el remitente 
    ipDestino=cbd.IPPersonal(aux[1]);                  
      
    return ipDestino;
}
public boolean cadenaPlayInfo(String cadena){
Pattern patron = Pattern.compile("^PLAYINFO:.+");
    Matcher mat = patron.matcher(cadena);
        return  mat.matches();
}


}