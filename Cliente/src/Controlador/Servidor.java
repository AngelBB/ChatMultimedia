package Controlador;

import Vista.PanelCliente;
import java.net.*;
import java.io.*;


public class Servidor extends Thread{
    
    //Ventana del chata privado
    VentanaPrivada ventana;
    
    //Socket
    private Socket socketPrivado;
    private ServerSocket socketServidor;
    private int puerto;
    
    //Nuestra interfaz
    private static PanelCliente interfazCliente;
        
    //Nuestro cliente principal
    Cliente cliente;
        
    //Nombre del la hebra
    private String nombreUsuario;
    
    //Nombre del usuario Propietario
    private String propietario;
    
    //Flujos E/S
    PrintWriter out;
    BufferedReader in;
    
   
    
    public Servidor(PanelCliente ps,Cliente cli,String propietario ,String user,int puerto){
        this.interfazCliente=ps;
        this.cliente=cli;
        this.nombreUsuario=user;
        this.puerto=puerto;
        this.propietario=propietario;
    }
    
    public String getNombreHebra(){
        return this.nombreUsuario;
    }
    
    public VentanaPrivada getVentanaPrivada(){
        return this.ventana;
    }
    
    public void desconectar(String cadena){
        this.cliente.setUsuarioABorrar(cadena);
    }
    
    public Cliente getCliente(){
       return this.cliente;
    }
    
    public void enviarDatos(String datos){
        try {
            if(datos!=null && !datos.isEmpty()){//Comprobamos que no mande una cadena vacia
                this.out.println(datos);//escribimos al servidor
                this.out.flush();//Para forzar a que se envien todos los datos
            }
        } catch (Exception ex) {
            interfazCliente.setLog(ex.getMessage());
        }
    }
    
    
    public void hablar(String cadena){
        cadena="MSG:"+cadena+"#FROM#"+this.propietario+"#TO#"+this.nombreUsuario;
        enviarDatos(cadena);
    }
    
     public void imprimirEnPantalla(String cadena){
        String [] aux=cadena.split("#");
        String [] msg=aux[0].split(":");
        ventana.setCaja_chatP2P(this.nombreUsuario+" -> "+msg[1]);
    }
    
    public void cerrarConexiones(){
        try {
            out.close();
            in.close();
            socketPrivado.close();
            socketServidor.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    } 
     
    public void iniciarServidor (){
        try{
            //socketServidor = new ServerSocket(puerto);//le pasamos el parametro del puerto a utilizar en la clase ServerSocket
            //interfazCliente.setLog("Servidor iniciado.\nEscuchando por puerto: "+this.puerto);
            //Socket socketPrivado=socketServidor.accept();//accept devuelve el socketcliente
            String inputLine, outputLine;
            
            this.socketServidor = new ServerSocket(puerto);//le pasamos el parametro del puerto a utilizar en la clase ServerSocket  
            this.socketPrivado=socketServidor.accept();//accept devuelve el socketPrivado
            
            //Instanciamos lo flujos de I/O
            this.out = new PrintWriter(this.socketPrivado.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socketPrivado.getInputStream()));
            PrtMultimedia protocolo = new PrtMultimedia();//Instanciamos nuestra clase protocolo
                       
            
            ////////////////////////////////////////////////////////////////////
            //Ahora tendriamos que abrir una nueva ventana con el chat privado//
            ////////////////////////////////////////////////////////////////////
            this.ventana=new VentanaPrivada();//Instanciamos la ventana
            this.ventana.setServidor(this);//Le pasamos nuestra esta misma clase
            this.ventana.setTitle(this.nombreUsuario);//le ponemos el nombre a la ventana
            this.ventana.setVisible(true);//la hacemos visible
            
            //dispose();
            
            
            //Para todos los mensaje que se vallan recibiendo los procesamos 
            while((inputLine = in.readLine()) != null){
                if(protocolo.esMensaje(inputLine)){
                    imprimirEnPantalla(inputLine);
                }
                
               /* if(protocolo.desconexion(inputLine)){
                    ventana.setCaja_chatP2P(this.nombreUsuario+" se ha desconectado");
                }*/
            }
            
            out.close();
            in.close();
            socketPrivado.close();
            socketServidor.close();
        }catch(IOException ex){
            interfazCliente.setLog("No se puede escuchar por el puerto :"+this.puerto+"\n"+ex.getMessage());
        }
    }
        
    
    @Override
    public void run(){
        iniciarServidor();
    }
}