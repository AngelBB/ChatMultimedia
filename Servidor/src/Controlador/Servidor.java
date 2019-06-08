package Controlador;

import Vista.PanelServidor;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
//import java.util.concurrent.Semaphore;

public class Servidor extends Thread{
    
    //Puerto y socket servidor
    private int puerto;
    private ServerSocket socketServidor;
    
    //Para el bucle infinito
    private boolean escuchaActiva=true;
          
    //Para el id de la hebra
    int contador=0;
    
    //Vista 
    private static PanelServidor interfazServidor;
    
    //Flujo de entrada
    BufferedReader in;
    
    //Para guardar la lista de hebras clientes
    private ArrayList<ServidorThread> listaClientes=new ArrayList();
        
    //Semaforos
    /*static Semaphore  escribir=new Semaphore(1,true),
               leer=new Semaphore(0,true),
               mutex=new Semaphore(1,true);*/
        
    public Servidor(PanelServidor ps){
        interfazServidor=ps;
    }
    
    public Servidor(int port) {
        puerto=port;
    }
    
    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }
    
    
    public static synchronized void setLog(String cadena){
        try{
            interfazServidor.setLog(cadena);
        }catch(Exception ex){
            interfazServidor.setLog(cadena);
        }
    }
        
    
    public void cerrarConexiones(){
        try{
            escuchaActiva=false;
            socketServidor.close();//Para cerrar el socket del servidor 
            //Para borrar el array de usuarios
            listaClientes=null;//lo machacamos a pelo
            interfazServidor.setLog("Servidor detenido");
        }catch(IOException ex){
            interfazServidor.setLog("No se ha podido cerrar las conexiones: "+ex.getMessage());
        }
    }
    
    public void iniciarServidor(){
        try{
            socketServidor = new ServerSocket(puerto);//le pasamos el parametro del puerto a utilizar en la clase ServerSocket
            interfazServidor.setLog("Servidor iniciado.\nEscuchando por puerto: "+this.puerto);
            //Empieza a escuchar todas las peticiones entrantes
            while (escuchaActiva){
                Socket socket=socketServidor.accept();//accept devuelve el socketcliente
                interfazServidor.setLog("Peticion entrante: "+socket.getInetAddress().toString());
                ServidorThread st=new ServidorThread(contador++,this,socket,interfazServidor);
                st.start();
                listaClientes.add(st);//añadimos los thread a un array
            }
        }catch(IOException ex){
            interfazServidor.setLog("No se puede escuchar por el puerto :"+this.puerto+"\n"+ex.getMessage());
        }
    }
    
    public synchronized ArrayList<ServidorThread> getArrayClientes(){
        return this.listaClientes;
    }
    
    public synchronized void borrarCliente(int idHebra){
        for(int i=0;i<getArrayClientes().size();i++){
            if(getArrayClientes().get(i).getIdCliente()== idHebra){//comprobamos que sea la hebra indicada
                getArrayClientes().get(i).cerrarConexion();//Cerramos la conexion y los flujos de i/o
                getArrayClientes().remove(i);//Borramos la hebra por completo
                break;
            }
        }
    }
    
    
    @Override
    public void run(){
        iniciarServidor();
    }
}