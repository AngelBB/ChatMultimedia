package Controlador;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class PrtMultimedia {
   
    //private final ConectarBD cbd=new ConectarBD();
           
            
public PrtMultimedia(){}

public boolean esMensaje(String cadena){
    boolean control=false;
    Pattern patron=Pattern.compile("^MSG:.+");//Para comprobar el formato de la cadena que sea el correcto
    Matcher mat = patron.matcher(cadena);  
    
    if(mat.matches()){
        control=true;
    }
   
    return control;
}   
/*public boolean desconexion(String cadena){
    boolean control=false;
    Pattern patron=Pattern.compile("OK#DISCONNECTED.+");
    Matcher mat = patron.matcher(cadena);  
    
    if(mat.matches()){
        control=true;
    }

    return control;
}*/
    

}
