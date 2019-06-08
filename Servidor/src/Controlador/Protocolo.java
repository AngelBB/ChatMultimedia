package Controlador;

import Modelo.ConectarBD;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocolo {
    private final ServidorThread st;
    private final ConectarBD cbd=new ConectarBD();
           
            
    public Protocolo(ServidorThread st){
        this.st=st;
        this.cbd.iniciarConexion();
    }
        
            
    public String processInput(String entrada) {
        String salida = null;
        
        //LOGIN
        Pattern patron=Pattern.compile("^CON:.+");//Para comprobar el formato de la cadena que sea el correcto
        Matcher mat = patron.matcher(entrada);
         
        if(mat.matches()){
            if(validarLogin(entrada)){
                salida="OK#CONNECTED";
            }else{
                salida="ERROR#INVALID_CREDENTIALS";
            }
        }
        
        //LISTA USUARIOS
        if(entrada.equals("LSUSER")){
            salida=generarListaUsuarios();
        }
                        
        return salida;
    }

    public boolean validarCadenaDesconexion(String cadena){
    Pattern patron=Pattern.compile("^DISCONNECT:.+");//Para comprobar el formato de la cadena que sea el correcto
    Matcher mat = patron.matcher(cadena);

        return mat.matches();
    }

    public boolean validarLogin(String cadena){
        Boolean devolver=false;

        if(!cadena.equals("")){//Comprobamos que no este vacia
            Pattern patron=Pattern.compile("^CON:\\[.+\\]\\[.+\\]");//Para comprobar el formato de la cadena que sea el correcto
            Matcher mat = patron.matcher(cadena);
            if(mat.matches()){//Si la cadena cumple con el patron establecido
                //Tenemos que dividir la cadena
                String aux1=cadena.replace("[",",");
                String aux2=aux1.replace("]","");
                String aux3=aux2.replace(","," "); 
                String[] resultado = aux3.split(" "); 

                //Sacamos el usuario y la contrasenia
                String usuario=resultado[1];
                String contrasenia=resultado[2];

                //cbd.iniciarConexion();
                devolver=cbd.loguearUsuario(st.getSocket().getInetAddress().toString(), usuario, contrasenia);

            }//if
        }//if

        return devolver;
    }

    public boolean comprobarPeticionP2P(String cadena){
        //CONPOINT:[LOGIN_ORIGEN][LOGIN_DESTINO][IP][PUERTO]
        boolean control=false;
        Pattern patron=Pattern.compile("^CONPOINT:.+");//Para comprobar el formato de la cadena que sea el correcto
        Matcher mat = patron.matcher(cadena);  

        if(mat.matches()){
            control=true;
        }
        return control;
    }

    public String comprobarDestinatarioP2P(String cadena){
        //CONPOINT:[LOGIN_ORIGEN][LOGIN_DESTINO][IP][PUERTO]
        String aux1=cadena.replace("[",",");
        String aux2=aux1.replace("]","");
        String aux3=aux2.replace(","," "); 
        String[] resultado = aux3.split(" "); 

        String ip=cbd.IPPersonal(resultado[2]);
        return ip;
    }

    public String generarListaUsuarios(){
        String cadena="";
        cadena=cbd.listaUsuarios();

        return cadena;
    }

    public String desconexion(String cadena){
        //HAY QUE CAMBIAR ESTE METOD POR COPLETO

        String devolver=null;

        if(!cadena.equals("")){//Comprobamos que no este vacia
            Pattern patron=Pattern.compile("^DISCONNECT:\\[.+\\]");//Para comprobar el formato de la cadena que sea el correcto
            Matcher mat = patron.matcher(cadena);
            if(mat.matches()){//Si la cadena cumple con el patron establecido
                //Tenemos que dividir la cadena
                String aux1=cadena.replace("[",",");
                String aux2=aux1.replace("]","");
                String aux3=aux2.replace(","," "); 
                String[] resultado = aux3.split(" "); 

                //Sacamos el usuario y la contrasenia
                String usuario=resultado[1];
                // Establecemos la conexión con la base de datos.
                //cbd.iniciarConexion();

                if(cbd.desconexion(usuario)){
                    devolver="OK#DISCONNECTED#"+usuario;
                }
             }//if
        }//if

        return devolver;
    }

    public boolean comprobarMensajeBroadcast(String cadena){
        boolean control=false;

        if(!cadena.equals("")){//Comprobamos que no este vacia
            Pattern patron1=Pattern.compile("^MSG:.+");//Para comprobar el formato de la cadena que sea el correcto
            Pattern patron2=Pattern.compile(".+#TO#BROADCAST$");//Para comprobar el formato de la cadena que sea el correcto

            Matcher mat1 = patron1.matcher(cadena);
            Matcher mat2 = patron2.matcher(cadena);

            if(mat1.matches()){//Si la cadena cumple con el patron establecido
                if(mat2.matches()){
                    control=true;
                }
            }
        }
        return control;
    }

    public boolean comprobarMensajePrivado(String cadena){
        boolean control=false;
        //CONPOINT:[LOGIN_ORIGEN][LOGIN_DESTINO][IP][PUERTO]

        if(!cadena.equals("")){//Comprobamos que no este vacia
            Pattern patron1=Pattern.compile("^MSG:.+");//Para comprobar el formato de la cadena que sea el correcto
            Pattern patron2=Pattern.compile(".+[^#TO#BROADCAST]$");//Para comprobar el formato de la cadena que sea el correcto

            Matcher mat1 = patron1.matcher(cadena);
            Matcher mat2 = patron2.matcher(cadena);

            if(mat1.matches()){//Si la cadena cumple con el patron establecido
                if(mat2.matches()){
                    control=true;
                }
            }
        }

        return control;
    }

    //Con este metodo sacaremos la ip a traves del nombre del usuario y enviaremos el mensaje hacia esa ip
    public String comprobarDestinatarioMensajePriv(String cadena){
        //String dir_ip=sk_cli.getInetAddress().toString();//Wathsapp
       //String dir_ip=sk_cli.getRemoteSocketAddress().toString();//stackoverflow
        String ipDestino=null;

        //MSG:mensaje#FROM#login_sender#TO#login_receiver
        String [] aux=cadena.split("#");//pos 3 destinatario - pos 2 el remitente 
        String [] msg=aux[0].split(":");//pos 1 mensaje

        //cbd.iniciarConexion();
        ipDestino=cbd.IPPersonal(aux[4]);                  

        return ipDestino;
    }

    //Con este metodo sacaremos la ip a traves del nombre del usuario y enviaremos el mensaje hacia esa ip
    public String comprobarRemitenteMensajePublico(String cadena){
        //String dir_ip=sk_cli.getInetAddress().toString();//Wathsapp
        //String dir_ip=sk_cli.getRemoteSocketAddress().toString();//stackoverflow
        String ipRemitente=null;

        //MSG:mensaje#FROM#login_sender#TO#BROADCAST
        String [] aux=cadena.split("#");//pos 3 destinatario - pos 2 el remitente 
        String [] msg=aux[0].split(":");//pos 1 mensaje o aviso de (null) para abrir nueva pestaña en el cliente

        //cbd.iniciarConexion();
        ipRemitente=cbd.IPPersonal(aux[2]);                  

        return ipRemitente;
    }

    public String comprobarUsuarioDesconectado(String nombreUsu){
        String ipRemitente=cbd.IPPersonal(nombreUsu);     
        return ipRemitente;
    }
}
