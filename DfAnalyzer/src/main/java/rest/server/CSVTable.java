/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Débora
 */
class CSVTable {
    public String csv;
    public List<List<String>> rows = new  ArrayList<>();
    public List<String> cols = new  ArrayList<>();
    
    public CSVTable(String csv) throws IOException {
        this.csv = csv;
        String currentPath = System.getProperty("user.dir");
        String file = currentPath + "/query_result.csv";
        BufferedReader input = null;
        int i = 0;
        try 
        {
            input =  new BufferedReader(new FileReader(file));
            String line = null;
            while (( line = input.readLine()) != null)
            {
                String[] data = line.split(";");   
                if (i==0){
                    for (String a: data){
                        this.cols.add(a.replace("\"",""));   
                    }
                    i = 1;
                }
                else{
                    for (int j=0; j<data.length; j++){
                        data[j] = data[j].replace("\"","");
                    }
                    ArrayList<String> items = new  ArrayList<>(Arrays.asList(data)); 
                    this.rows.add(items);
                }  
            }
        }
        catch (Exception ex)
        {
              ex.printStackTrace();
        }
        finally 
        {
            if(input != null)
            {
                input.close();
            }
        }
    }        
    
    public List<List<String>> rows (){
        return rows;
    }
    public List<String> cols(){
        return cols;
    }
    public String csv(){
        return csv;
    }
}