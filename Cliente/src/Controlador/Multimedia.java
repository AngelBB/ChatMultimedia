/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Vista.PanelCliente;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


/**
 *
 * @author 2dai
 */
public class Multimedia {
    
    //Clases
    PanelCliente interfazCliente;
    Cliente cli;

    //Cadenas
    String nombreArchivo, usuarioDestinatario, nombreArchivoRecibido, nombreArchivoQueQueremos; 
    
    //Tamaño de la cancion recibida
    long tamanioArchivoRecibido;
    
    //Array de ficheros y rutas
    ArrayList<File> archivos=new ArrayList();
    File directorioSubida,directorioDescargas;
    
    //Herramientas nativas java 8 para codificar y decodificar en base64 
    java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
    java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
    
    //Para repdoducir musica
    FileInputStream fis;
    Player player;
    BufferedInputStream bis;
    
    //Para determinar el tipo de archivo
    Pattern patronCanciones=Pattern.compile(".+\\.mp3$");
    Pattern patronVideo=Pattern.compile(".+\\.avi$");
    Pattern patronImagenes=Pattern.compile(".+\\.jpg$");
    
    //Contador para la barra de progreso
    long contStreaming=0;//para llevar la cuenta de los paquetes recibidos e iniciar el streaming
    int num=0;//para pasar a la barra de progreso
    int contBarra=0;//Para la barra de progreso
    
    //Para reproducir la cancion 
    Thread cancion;
    
    //Cuentas para controlar el straming y el vaciado de variables 
    int totalpaquetesrecibidos;
    int unoPorCiento;
    int empezarReproducir;
    
    
    //Constructor
    public Multimedia(PanelCliente pc,Cliente cl){
        this.interfazCliente=pc;
        this.cli=cl;
    }
    
    public void resetearContadores(){
        this.contStreaming=0;//para llevar la cuenta de los paquetes recibidos e iniciar el streaming
        this.num=0;//para pasar a la barra de progreso
        this.contBarra=0;
        this.totalpaquetesrecibidos=0;
        this.unoPorCiento=0;
        this.empezarReproducir=0;
    }
    
    
    
    
    //Para realizar conversiones 
    public String bytearrayToString(byte[] bytearray) {
        String str = null;
        try {
            str = new String(bytearray, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
        }
        return str;
    }

    public byte[] stringToBytearray(String str) {
        byte[] bytearray = null;
        try {
            bytearray = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
        }
        return bytearray;
    }
    
    
    //Para enviar los paquetes de la cancion
    public void enviarPaquetesCancion(){
                try{
                    File archivoSeleccionado = null;//Cuidado con esto ya que puede pegar petardazo 
                    //Si no existe el directorio 
                    for(File archivo:this.directorioSubida.listFiles()){
                            if(archivo.getName().equals(this.nombreArchivo)){
                                archivoSeleccionado=archivo;
                            }
                    }

                    if(archivoSeleccionado!=null){
                        //Empezamos a trocear la cancion para enviarla
                        FileInputStream fis=new FileInputStream(archivoSeleccionado); 
                        byte[] trozoArchivo=new byte[64];//Un vector de bytes
                        while(fis.read(trozoArchivo)!=-1){//vamos guardando los bytes del archivo a trozos de 64 ya que nuestro array tiene 64 de tamaño
                            String trozo = bytearrayToString(encoder.encode(trozoArchivo));//Codificamos la cancion
                            //String msg="PLAYDATA:"+this.usuarioDestinatario+"#"+encoder.encodeToString(trozoArchivo)+"#"+this.cli.getUsuario();//formamos la cadena
                            String msg="PLAYDATA:"+this.usuarioDestinatario+"#"+trozo+"#"+this.cli.getUsuario();//formamos la cadena
                           this.cli.enviarDatos(msg);//vamos enviando
                        }
                        fis.close();//cerramos
                    }
                }catch(Exception ex){
                    interfazCliente.setLog(ex.getMessage());
                }
    }
    
    //Para recibir los paquetes de la cancion
    public void recibirPaquetesCancion(String cadena) {
       //Cuando se halla enviado el archivo completo reseteamos los contadores
            if(contStreaming>=totalpaquetesrecibidos){
                    resetearContadores();
                    interfazCliente.setBarraProgreso(0);
            }
        
        //PLAYDATA:login_destino#datos_binarios#UsuarioOrigen
        try{  
            String[] trozos=cadena.split("#");
            //byte[] Decodificador=decoder.decode(trozos[1]);
            byte[] Decodificador=decoder.decode(stringToBytearray(trozos[1]));//Decodificamos la cancion
            File fichero=new File(this.directorioDescargas.getAbsolutePath()+"\\"+this.nombreArchivoQueQueremos);//creamos nuestro fichero
            FileOutputStream flujo= new FileOutputStream(fichero,true);//abrimos el flujo
            flujo.write(Decodificador);//Lo vamos escribiendo en un fichero
            flujo.close();//cerramos el flujo.
            
            //Reproducion en streaming y barra de progreso
            contBarra++;
            contStreaming++;
            
            if(unoPorCiento==contBarra){//Barra de progreso
                contBarra=0;
                interfazCliente.setBarraProgreso(num++);//vamos pintando la barra de progreso
            }
            
            //Se supone que entrara una sola vez 
            if(empezarReproducir==contStreaming){//empezar a reproducir

                    Matcher matC = patronCanciones.matcher(this.nombreArchivoQueQueremos);//Musica
                    Matcher matV = patronVideo.matcher(this.nombreArchivoQueQueremos);//Video

                    //Canciones
                    if(matC.matches()){
                        this.fis = new FileInputStream(fichero);
                        this.bis = new BufferedInputStream(this.fis);
                        this.player = new Player(this.bis);//Llamada a constructor de la clase Player
                        //Cada vez que entremos se borrara el antiguo hilo y empezaremos a reproducir otro
                        this.cancion= new Thread() {
                            @Override
                            public void run() {
                                try {
                                    player.play();//Llamada al método play
                                } catch (JavaLayerException ex) {
                                    System.err.println(ex.getMessage());
                                }
                            };
                        };
                        cancion.start();
                    }
                        
                    //Video
                    if(matV.matches()){
                        VentanaVideo vd=new VentanaVideo(fichero.getName(),fichero.getPath());
                    }
            }//if del streaming
            
            //Imagenes
            //La imagen la visualizamos una vez descargada
            Matcher matI = patronImagenes.matcher(this.nombreArchivoQueQueremos);//Imagen
            if(totalpaquetesrecibidos==contStreaming && matI.matches()){
            //if(fichero.length() == this.tamanioArchivoRecibido && matI.matches()){ El tamaño a veces varia y no se cumple la condicion
                    VentanaImagen im=new VentanaImagen();
                    im.setUrl(fichero.toURI().toURL());
                    im.setTitle(fichero.getName());
                    im.setVisible(true);
            }
                        
        }catch(NumberFormatException | IOException  | JavaLayerException ex){
            interfazCliente.setLog(ex.getMessage());
        }
    }
    
    //Para enviar el playinfo de una cancion que nos solicitan
    public void cancionSolicitada(String cadena){
        //PLAY:nombre canción#login_origen#login_destino
        String [] aux1=cadena.split("#");
        String [] aux2=aux1[0].split(":");//En aux2[1] esta el titulo de la cancion
        this.nombreArchivo=aux2[1];
        this.usuarioDestinatario=aux1[1];
            
        for(File archivo:this.directorioSubida.listFiles()){
                    if(archivo.getName().equals(this.nombreArchivo)){
                        archivo.length();
                        cli.enviarDatos("PLAYINFO:"+archivo.length()+"#"+aux1[1]);
                    }
        }
    }
    
    //Guardamos la info del play info que recibimos
    public void guardarInfoCancion(String cadena){
        //PLAYINFO:tamanio#login_destino
        String [] aux1=cadena.split("#");
        String [] tamanio=aux1[0].split(":");
        this.tamanioArchivoRecibido=Integer.parseInt(tamanio[1]);
        this.totalpaquetesrecibidos=(int)this.tamanioArchivoRecibido/64;//Sacar el total de paquetes
        this.unoPorCiento=(int)(totalpaquetesrecibidos/100);//El uno por ciento de los paquetes recibidos para la barra de progreso
        this.empezarReproducir=(int)(totalpaquetesrecibidos/100)*20;//Para empezar a reproducir a partir del 20 por ciento
        
    }
    
    //Lista de canciones
    public void pedirListaCanciones(String usuarioSeleccionado){
        String cadena="SONGLIST:FROM#"+cli.getUsuario()+"#TO#"+usuarioSeleccionado;
        cli.enviarDatos(cadena);
    } 
        
    //Enviar lista
    public void compartirMisCanciones(String cadena) {
       //Sacamos el usuario que nos lo envia    
       //SONGLIST:FROM#login_sender#TO#login_receiver
        String [] usuarioCliente=cadena.split("#");
        String enviar="LIST:FROM#"+cli.getUsuario()+"#TO#"+usuarioCliente[1];
        String[] ficheros = this.directorioSubida.list();
        String sFichero="";
                     
        if (ficheros == null){
            interfazCliente.setLog("No hay ficheros en el directorio especificado");
        } else if(ficheros.length>0){
                for (int x=0;x<ficheros.length;x++){
                    sFichero = directorioSubida.getAbsolutePath()+ficheros[x];
                    archivos.add(new File(sFichero));
                    enviar=enviar.concat("#"+ficheros[x]);
                } 
        }else{
            enviar+="No hay canciones disponibles";
        }
        
        cli.enviarDatos(enviar);
    } 	
    
    //Recibir lista
    public void guardarListaDeCancionesRecibida(String cadena){
        //LIST:FROM#login_sender#TO#login_receiver#cancion1#cancion2
        String[]aux=cadena.split("#");
        DefaultListModel modelo = new DefaultListModel();
        for(int i=4;i<aux.length;i++){
            modelo.addElement(aux[i]);        
        }
        interfazCliente.setListaCanciones(modelo);
        interfazCliente.repaint();
    }
    
    //Mandar el aviso para reproducir la cancion
    public void iniciarReproduccion(String nombreCancion,String usuarioLogueado,String usuarioDeLaCancion){
        this.nombreArchivoQueQueremos=nombreCancion;
        String cadena="PLAY:"+nombreCancion+"#"+usuarioLogueado+"#"+usuarioDeLaCancion;
        cli.enviarDatos(cadena);
    }
        
    //Directorio de subida
    public File getDirectorioSubida(){
        return this.directorioSubida;
    }
    public void setDirectorioSubida(JFileChooser archivo){
        this.directorioSubida=archivo.getSelectedFile();
    }
    
    //Directorio de bajada
    public void setDirectorioBajada(JFileChooser archivo){
        this.directorioDescargas=archivo.getSelectedFile();
    }
    public File getDirectorioDescargas(){
        return this.directorioDescargas;
    }
    
    //Coger array de archivos del directorio
    public ArrayList getArchivos(){
        return this.archivos;
    }
    
   
}