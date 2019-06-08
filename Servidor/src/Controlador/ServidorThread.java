package Controlador;

import Vista.PanelServidor;
import java.net.*;
import java.io.*;
 
public class ServidorThread extends Thread {
    int id;//id de esta hebra
    private Socket socket;//socket
    
    //Flujos de IO
    PrintWriter out;
    BufferedReader in;
    
    //Clase padre
    Servidor serv;
    
    //Interfaz grafica
    PanelServidor interfazServidor;
    
    public ServidorThread(int id, Servidor serv, Socket socket, PanelServidor ps) {
        this.serv = serv;
        this.socket = socket;
        this.interfazServidor = ps;
    }
    
    public Socket getSocket(){
        return this.socket;
    }
 
    public void enviarDatos(String cadena){
        this.out.println(cadena);
    }
    
    public void cerrarConexion(){
        try{
            out.close();
            in.close();
            socket.close();
        }catch(Exception ex){
            interfazServidor.setLog(ex.getMessage());
        }
    }

    public int getIdCliente() {
        return id;
    }
    
    public Servidor getServidor(){
        return this.serv;
    }
       
    @Override
    public void run() {
        try {
            //Para la salida de datos
            out = new PrintWriter(socket.getOutputStream(), true);

            //creamos un buffer con los datos de llegada
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine, outputLine;

            //Instanciamos nuestros protocolos
            Protocolo prt = new Protocolo(this);//Para conversaciones
            ProtocoloCanciones prtc= new ProtocoloCanciones();//Para canciones

                while((inputLine = in.readLine()) != null){
                    interfazServidor.setLog(inputLine); //Para mostrar por el log del servidor

                    if(prt.comprobarMensajeBroadcast(inputLine)){
                        //BROADCAST;
                        PrintWriter outB;
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                                //Enviamos la cadena en el formato establecido de
                            if(cliente.getSocket().getInetAddress().toString().equals(prt.comprobarRemitenteMensajePublico(inputLine))){
                                //No le envio nada al remitente
                            }else{
                                //a los demas se lo envio
                                outB= new PrintWriter(cliente.getSocket().getOutputStream(), true);         
                                outB.println(inputLine);
                                outB.flush();
                            }
                        }
                    }else if(prt.comprobarMensajePrivado(inputLine)){
                        //MENSAJE PRIVADO  
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //AVISO : SI ESTAMOS EN LOCAL SE REENVIA EL MENSAJE POR DOBLE . Hay que probarlo aparte
                            //Al comprobarlos los dos a la vez siempre entrarán porque las cadenas estan bien formadas .
                            //entonces enviara el mensaje tantas veces como sockets haya.

                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prt.comprobarDestinatarioMensajePriv(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }else if(prt.comprobarPeticionP2P(inputLine)){  
                        //PETICION P2P
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prt.comprobarDestinatarioP2P(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }else if(prtc.cadenaSonglist(inputLine)){
                        //SONGLIST
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prtc.comprobarDestinatarioSonglist(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }
                    else if(prtc.cadenaPlay(inputLine)){
                        //PLAY
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prtc.comprobarPlayCancion(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }
                    else if(prtc.cadenaList(inputLine)){
                        //LIST
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prtc.comprobarDestinatarioList(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }
                    else if(prtc.cadenaPlayData(inputLine)){
                        //PLAYDATA
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prtc.reenviarPlayData(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }
                    else if(prtc.cadenaPlayInfo(inputLine)){
                        //PLAYINFO
                        for (ServidorThread cliente : serv.getArrayClientes()) {
                            //Para el destinatario
                            if (cliente.getSocket().getInetAddress().toString().equals(prtc.enviarPlayInfo(inputLine))){
                                PrintWriter outP = new PrintWriter(cliente.getSocket().getOutputStream(), true);
                                outP.println(inputLine);
                            }
                        }
                    }else if(prt.validarCadenaDesconexion(inputLine)){
                        //DESCONEXION
                        String aviso=prt.desconexion(inputLine);
                        interfazServidor.setLog(aviso);
                        out.println(aviso);//Enviamos mensaje de desconexion al user
                        this.serv.borrarCliente(this.getIdCliente());//procedemos a borrar la hebra
                    }else{
                        //PARA EL RESTO DE PETICIONES
                        outputLine = prt.processInput(inputLine);//procesamos la respuesta entrante con el protocolo
                        interfazServidor.setLog(outputLine);                            
                        out.println(outputLine);//y enviamos la respuesta
                    }
                }
        } catch (IOException e) {
            interfazServidor.setLog(e.getMessage());  
        }
    }
}