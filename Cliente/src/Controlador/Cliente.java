/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;


import Vista.PanelCliente;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;


/**
 *
 * @author 2dai
 */
public class Cliente extends Thread {
    //Para la conexion
    private Socket socketCliente;
    private PrintWriter out ;
    private BufferedReader in;
    PanelCliente interfazCliente;
       
    //Para el chat punto a punto
    ArrayList<Servidor> ArrayServidor=new ArrayList();
    ArrayList<ServidorDirecto> ArrayServidorD=new ArrayList();
    String usuarioABorrarD;//servidor directo
    String usuarioABorrar;//servidor
    
    //Para las canciones
    Multimedia mtmd;
    
    //Variables con datos
    private String usuario,pass,usuarioDestino;//usuario,pass,log
    private boolean interruptor;//para el bucle
    
    //Para enviar la cancion
    Thread hilo;
    
    
    //Constructor
    public Cliente(PanelCliente pc){
        this.interfazCliente=pc;
        this.mtmd=new Multimedia(pc,this);
    }
      
    
    //Metodos GET - SET
    public String getUsuario(){
       return this.usuario;
    }
    public String getUsuarioDestino(){
        return this.usuarioDestino;
    }
    public void setInterruptor(boolean intr){
        this.interruptor=intr;
    }
    public boolean getInterruptor(){
        return this.interruptor;
    }
    public Multimedia getMultimedia(){
        return this.mtmd;
    }
       
    
    //*********************** METODOS BASICOS **********************************
    //Para crear la conexion e inicializar las variables de conexion user y pass
    public void conexion(String ipMaquina,int numeroPuerto,String usuario,String pass){
        try {
            //inicializamos 
            this.socketCliente = new Socket(ipMaquina, numeroPuerto);//Inicializamos el socket
            this.out = new PrintWriter(socketCliente.getOutputStream(), true);//Salida hacia el servidor
            this.in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));//Entrada del servidor
            this.usuario=usuario;
            this.pass=pass;
            this.interruptor=true;
            
        } catch (Exception ex) {
            interfazCliente.setLog("Error al abrir los sockets cliente " + ex.getMessage());
        }
    }
    
    //Para enviar los datos
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
    
    //Para enviar la desconexion
    public void desconectar(){
            enviarDatos("DISCONNECT:["+usuario+"]");
    }
    
    //Para enviar el login
    public void login(){
        //Lo enviamos según el protocolo
        String cadena="CON:["+this.usuario+"]["+this.pass+"]";
        enviarDatos(cadena);
    }    
     

    /*--------------------------------------------------------------------------
    ------------------------------ METODOS ESPECIFICICOS -----------------------
    ----------------------------------------------------------------------------*/
        
    //****************************** CHAT GRUPAL *******************************
    public void recibirChatGrupal(String cadena){
        String [] aux=cadena.split("#");
        String [] msg=aux[0].split(":");
        try{
            this.interfazCliente.setCajaChatGrupal(aux[2]+"->"+msg[1]);
        } catch (Exception ex) {
            this.interfazCliente.setLog(ex.getMessage());
        }
    }
    public void hablarChatGrupal(String cadena){
        String paraServer="MSG:"+cadena+"#FROM#"+this.usuario+"#TO#BROADCAST";
        enviarDatos(paraServer);
    }
     
    
    //****************************** CHAT PRIVADO ******************************
    public  void recibirChatPrivado(String cadena){
        //MSG:null#FROM#login_sender#TO#login_receiver
        //MSG:null#FROM#angel#TO#paco
        String [] aux=cadena.split("#");
        String [] msg=aux[0].split(":");
            try{
                if(msg[1].equals("null")){
                    this.interfazCliente.getPanelPestanias().addTab(aux[2],null, this.interfazCliente.getChatPrivado());
                    this.mtmd.pedirListaCanciones(aux[2]);//Pedimos las canciones del usuario que nos llama
                    
                    //ANTENCION: SI REENVIABA EN MSG A MI MISMO ESTO DARIA ERROR
                    //SOLUCIONADO EN LA PARTE DEL SERVIDOR
                    this.interfazCliente.setNombreUsuario(aux[2]);
                    this.interfazCliente.setNombrePropietario(this.usuario);
                    
                }else{
                    this.interfazCliente.setCajaChatIndividual(aux[2]+" -> "+msg[1]);
                    
                    //Pongo esta linea por seguridad por si no se ha creado la pestaña que por lo menos tenga el nombre del usuario
                    this.interfazCliente.setNombreUsuario(aux[2]);//Esta linea es importante ya que sin ella no tendriamos el usuario destino al
                    //que enviar el mensaje por lo tanto no se enviaria el mensaje privado.
                    this.interfazCliente.setNombrePropietario(this.usuario);
                }
                
            } catch (Exception ex) {
                interfazCliente.setLog(ex.getMessage());
            }
    }
    public void iniciarChatPrivado(String nomUsuDestino){
        String paraServer="MSG:"+null+"#FROM#"+this.usuario+"#TO#"+nomUsuDestino;
        enviarDatos(paraServer);
    }
    public void hablarChatPrivado(String cadena,String nomUsuDestino){
        String paraServer="MSG:"+cadena+"#FROM#"+this.usuario+"#TO#"+nomUsuDestino;
        enviarDatos(paraServer);
    }
       
    
    //****************************** LISTA USUARIOS ****************************
    public void guardarListaUsuarios(String cadena){
        DefaultListModel modelo = new DefaultListModel();
        String[]aux=cadena.split("#");
            for(int i=1;i<aux.length;i++){//Nos saltamos la primera posicion que es el ok
               if(!aux[i].equals(this.usuario)){
                    modelo.addElement(aux[i]);
               }
            }
            this.interfazCliente.setListaUsuarios(modelo);
    }
    public void pedirListaUsuarios(){
        enviarDatos("LSUSER");//Pedimos la lista de usuarios
    }
        
            
    //****************************** CHAT P2P **********************************
    public void inciarConexionChatP2P(String nomUsuario){
        //CONPOINT:[LOGIN_ORIGEN][LOGIN_DESTINO][IP][PUERTO]
        int puertoAleatorio=(int)(Math.random()*(50000-40000));
        
        //Para la direccion ip
        try{
            InetAddress localHost=InetAddress.getLocalHost();;
           
            //Nota el puerto lo vamos a poner random 
            String cadena="CONPOINT:["+this.usuario+"]["+nomUsuario+"]["+localHost.getHostAddress()+"]["+puertoAleatorio+"]";

            //Creamos el servidor 

            Servidor s=new Servidor(this.interfazCliente,this,this.usuario,nomUsuario,puertoAleatorio);
            this.ArrayServidor.add(s);//lo añadimos a un array de clientes
            s.start();//iniciamos el hilo

            enviarDatos(cadena);//enviamos la cadena 
        
        }catch(UnknownHostException ex){System.err.println("Host desconocido.");}   
    }
    public void recibirConPoint(String cadena){
        String aux1=cadena.replace("[",",");
        String aux2=aux1.replace("]","");
        String aux3=aux2.replace(","," "); 
        String[] resultado = aux3.split(" "); 
               
        //CONPOINT:[LOGIN_ORIGEN][LOGIN_DESTINO][IP][PUERTO]
        ServidorDirecto sd=new ServidorDirecto(this.interfazCliente,
                                                this,
                                                this.usuario,
                                                resultado[1],
                                                resultado[3],
                                                Integer.parseInt(resultado[4]));
        this.ArrayServidorD.add(sd);//lo añadimos a un array de clientes
        sd.start();//iniciamos el hilo
    }
    public void borrarPersonaP2P(){
        //Para servidor 
        if(this.usuarioABorrar!=null && !this.usuarioABorrar.isEmpty()){
            for(int i=0;i<this.ArrayServidor.size() ;i++){
                if(this.ArrayServidor.get(i).getNombreHebra().equals(this.usuarioABorrar)){
                    this.ArrayServidor.get(i).getVentanaPrivada().dispose();
                    this.ArrayServidor.get(i).cerrarConexiones();
                    this.ArrayServidor.remove(i);
                    this.usuarioABorrar=null;
                    break;
                }
            }
        }
        
        //Para servidor directo
        if(this.usuarioABorrarD!=null && !this.usuarioABorrarD.isEmpty()){
            for(int i=0;i<this.ArrayServidorD.size();i++){
                if(this.ArrayServidorD.get(i).getNombreHebra().equals(this.usuarioABorrarD)){
                    this.ArrayServidorD.get(i).getVentanaPrivada().dispose();
                    this.ArrayServidorD.get(i).cerrarConexiones();
                    this.ArrayServidorD.remove(i);
                    this.usuarioABorrarD=null;
                    break;
                }
            }
        }
    }
    public synchronized void setUsuarioABorrar(String cadena){
        this.usuarioABorrar=cadena;
    }
    public synchronized void setUsuarioABorrarD(String cadena){
        this.usuarioABorrarD=cadena;
    }
    
    
    //****************************** METODO PRINCIPAL **************************
    public void escucharServidor(){
    String cadena;    
    //Chats
    Pattern patron1=Pattern.compile("^MSG:.+");//Para comprobar el formato de la cadena que sea el correcto
    Pattern patron2=Pattern.compile(".+[^#TO#BROADCAST]$");//Para comprobar el formato de la cadena que sea el correcto
    Pattern patron3=Pattern.compile(".+#TO#BROADCAST$");//Para comprobar el formato de la cadena que sea el correcto
    Pattern patron4=Pattern.compile("^OK#DISCONNECTED#"+this.usuario);
        
    //Lista de canciones
    Pattern patronListSong=Pattern.compile("^LIST:FROM.+");//Para comprobar el formato de la cadena que sea el correcto
    
    //Lista de usuarios
    Pattern patronUS=Pattern.compile("^OK#[^DISCONNECTED].+");//Que no contenga disconnected
    
    //Paquetes entrantes de canciones
    Pattern patronPaqueteEntrante=Pattern.compile("^PLAYDATA:.+");
    
    //Paquete playInfo
    Pattern patronPlayInfo=Pattern.compile("^PLAYINFO:.+");  
    
    //Para empezar la transmision de paquetes
    Pattern patronRecibirPlay=Pattern.compile("^PLAY:.+");
    
    //para enviar la mi lista de canciones cuando la piden con: SONGLIST
    Pattern patronSongList=Pattern.compile("^SONGLIST:FROM.+");
    
    //Para comprobar el conpoint
    Pattern conpoint=Pattern.compile("CONPOINT:.+");
    
        while(this.interruptor){
            
            try{
                while((cadena=in.readLine())!=null){
                    
                    Matcher mat1 = patron1.matcher(cadena);
                    Matcher mat2 = patron2.matcher(cadena);
                    Matcher mat3 = patron3.matcher(cadena);
                    Matcher mat4 = patronUS.matcher(cadena);
                    Matcher mat5 = patron4.matcher(cadena);
                    Matcher mat6 = patronListSong.matcher(cadena);
                    Matcher mat7 = patronPaqueteEntrante.matcher(cadena);
                    Matcher mat8 = patronRecibirPlay.matcher(cadena);
                    Matcher mat9 = patronPlayInfo.matcher(cadena);
                    Matcher mat10 = patronSongList.matcher(cadena);
                    Matcher mat11 = conpoint.matcher(cadena);
                    
                    //Mensaje privado
                    if(mat1.matches() && mat2.matches()){
                        this.interfazCliente.setLog(cadena);
                        recibirChatPrivado(cadena);
                    }
                    
                    //Mensaje grupal
                    if(mat1.matches() && mat3.matches()){
                        this.interfazCliente.setLog(cadena);
                        recibirChatGrupal(cadena);
                    }

                    //Login
                    if(cadena.equals("OK#CONNECTED")){
                        this.interfazCliente.setLog("Estableciendo Conexion\n....\nConexion establecida correctamente");
                       
                    }else if(cadena.equals("ERROR#INVALID_CREDENTIALS")){
                        this.interfazCliente.setLog("Error al loguearse. Comprueba los datos");
                    }
                    
                    //Lista de usuarios
                    if(mat4.matches()){
                        this.interfazCliente.setLog(cadena);
                        guardarListaUsuarios(cadena);
                    }
                        
                    //Desconexion
                    if(mat5.matches()){
                        this.interfazCliente.setLog(cadena);
                        //Cerramos los flujos y el socket
                        this.in.close();
                        this.out.close();
                        this.socketCliente.close();
                        this.interruptor=false;
                    }
                    
                    //lista de canciones
                    if(mat6.matches()){
                        this.mtmd.guardarListaDeCancionesRecibida(cadena);
                    }
                    
                    //Playdata - Paquetes cancion entrantes
                    if(mat7.matches()){
                        this.mtmd.recibirPaquetesCancion(cadena);
                    }
                    
                    //Play - Paquetes cancion salientes
                    if(mat8.matches()){
                        this.mtmd.cancionSolicitada(cadena);//le enviamos el playinfo 
                        this.hilo=null;
                        this.hilo=new Thread() {
                            @Override
                            public void run() {
                                mtmd.enviarPaquetesCancion();
                            };
                        };       
                        this.hilo.start();
                    }
                    
                    //Cuando recibo el playinfo de una cancion
                    if(mat9.matches()){
                        this.mtmd.guardarInfoCancion(cadena);
                    }
                    
                    //Enviar mi List de canciones
                    if(mat10.matches()){
                        this.mtmd.compartirMisCanciones(cadena);
                    }
                    
                    //Cuanto recibimos el conpoint
                    if(mat11.matches()){
                        recibirConPoint(cadena);
                    }
                }   
            }catch(Exception ex){
                this.interfazCliente.setLog(ex.getMessage());
            }
        }
    } 
          
    
    //Metodo RUN
    @Override
    public void run(){
        escucharServidor();
    }
}