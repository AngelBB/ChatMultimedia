package Controlador;

import Vista.PanelCliente;
import java.net.*;
import java.io.*;



public class ServidorDirecto extends Thread{
    
    //Ventana del chata privado
    VentanaPrivada ventana;
    
    //Socket
    private Socket socketPrivado;
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
      
    
    public ServidorDirecto(PanelCliente ps,Cliente cli,String propietario ,String user,String ip,int puerto){
        try {
            this.interfazCliente=ps;
            this.cliente=cli;
            this.nombreUsuario=user;
            this.puerto=puerto;
            this.socketPrivado=new Socket(ip,puerto);
            this.propietario=propietario;
        }catch(IOException ex){
            interfazCliente.setLog("Error al abrir socket del chat P2P");
        }
    }
    
    public VentanaPrivada getVentanaPrivada(){
        return this.ventana;
    }
    
    public String getNombreHebra(){
        return this.nombreUsuario;
    }
    
    public void desconectarD(String cadena){
        this.cliente.setUsuarioABorrarD(cadena);
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
    
    public Cliente getCliente(){
       return this.cliente;
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
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void iniciarServidor (){
        try{
            
            //socketServidor = new ServerSocket(puerto);//le pasamos el parametro del puerto a utilizar en la clase ServerSocket
            //interfazCliente.setLog("Servidor iniciado.\nEscuchando por puerto: "+this.puerto);
            //Socket socketPrivado=socketServidor.accept();//accept devuelve el socketcliente
            String inputLine;
              
            out = new PrintWriter(this.socketPrivado.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(this.socketPrivado.getInputStream()));
            PrtMultimedia protocolo = new PrtMultimedia();//Instanciamos nuestra clase protocolo
            
            //Enviamos la cadena de confirmacion para arrancar el servidor de origen
            out.println("OK#CONNECTED#POINT");
            
            ////////////////////////////////////////////////////////////////////
            //Ahora tendriamos que abrir una nueva ventana con el chat privado//
            ////////////////////////////////////////////////////////////////////
            this.ventana=new VentanaPrivada();//Instanciamos la ventana
            this.ventana.setServidorDirecto(this);//Le pasamos nuestra esta misma clase
            this.ventana.setTitle(this.nombreUsuario);//le ponemos el nombre a la ventana
            this.ventana.setVisible(true);//la hacemos visible
            //dispose();
                        
            //Para todos los mensaje que se vallan recibiendo los procesamos 
            while((inputLine = in.readLine()) != null){
                if(protocolo.esMensaje(inputLine)){
                    imprimirEnPantalla(inputLine);
                }
                
                /*if(protocolo.desconexion(inputLine)){
                    ventana.setCaja_chatP2P(this.nombreUsuario+" se ha desconectado");
                }*/
            }
            
            out.close();
            in.close();
            socketPrivado.close();
        }catch(IOException ex){
            interfazCliente.setLog("No se puede escuchar por el puerto :"+this.puerto+"\n"+ex.getMessage());
        }
    }
      
    
    
    @Override
    public void run(){
        iniciarServidor();
    }
}