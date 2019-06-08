/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author Angel
 */
public class ConectarBD {
    
    Connection on;
    Statement stmt ;
    
    public ConectarBD(){}
    
    public void iniciarConexion(){
        try{
            this.on=DriverManager.getConnection("jdbc:mysql://localhost:3306/spotify","root","");//despues de poner el puerto ponemos esto 
            this.stmt = on.createStatement();//Utilizamos nuestra clase de conexion y le pasamos el string
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
    }
    
    public Statement getStatement(){
        return this.stmt;
    }
    
    
    public boolean loguearUsuario(String ip,String usuario,String contrasenia){
        boolean control=false;
        try{
            String query= "SELECT * FROM users WHERE LOGIN='"+usuario+"' and PASS='"+contrasenia+"' AND ESTADO=0";
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                query="UPDATE users SET ESTADO=1, IP='"+ip+"' WHERE LOGIN='"+usuario+"' and PASS='"+contrasenia+"'";
                if(stmt.executeUpdate(query)>0){
                    control=true;//devolveremos true
                }
            }
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
       
        return control;
    }
    public String listaUsuarios(){
       ResultSet rs=null;
       String cadena="";
        try{
            String query= "SELECT LOGIN FROM users WHERE ESTADO=1";
            rs = stmt.executeQuery(query);
            cadena="OK#";
            if(rs !=null){
                while(rs.next()){
                    cadena = cadena.concat(rs.getString("LOGIN").concat("#"));
                }
            }
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
        
        return cadena;
    }
    public boolean desconexion(String usuario){
        boolean control=false;
        String query= "SELECT * FROM users WHERE LOGIN='"+usuario+"' and ESTADO=1";
        try{
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                query="UPDATE users SET ESTADO=0,IP='' WHERE LOGIN='"+usuario+"' and ESTADO=1";
                if(stmt.executeUpdate(query)>0){
                    control=true;
                }
            }
            
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }
        
        return control;
    }
    public String IPPersonal (String login){
        String query= "SELECT IP FROM users WHERE LOGIN='"+login+"' AND ESTADO=1";
        String ipDestino=null;
        try{
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                ipDestino=rs.getString("IP");
            }
            
        }catch(Exception ex){
            System.err.println(ex.getMessage());
        }

       return ipDestino;
    }
    
    
}