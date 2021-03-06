//PANAGIOTIS KONTOEIDIS
//1115201900266


import syntaxtree.*;
import visitor.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import javax.print.DocFlavor.STRING;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }
        FileInputStream fis = null;

        for(String file : args){

        try{
            fis = new FileInputStream(file);

            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("-----Program parsed successfully.-------");
            System.out.println();

            MyVisitor firstvisitor = new MyVisitor();  //fill the symboltable and do some basic type checking

            root.accept(firstvisitor, null);
            System.out.println("-----The Offsets are:-----\n");
            firstvisitor.st.printoffsets();          //print offsets
            System.out.println("----------------\n");

            MyVisitor2 secondvisitor = new MyVisitor2(firstvisitor);    //do a complete semantic analysis based on the symboltable from the first visitor

            VTable vt = new VTable(firstvisitor.st);        //create Virtual tables from the symboltable of the first visitor
        
            //vt.print();   //prints VTables
            
            String a[] = file.split(".java");
            String llname = a[0]+".ll";
            File llfile = new File(llname);

            if (llfile.createNewFile())
                System.out.println("New file created");
            else
                System.out.println("File already exists");

            System.out.println("\n---Intermediate Code Generation Started---\n");
            
            MyVisitor3 thirdvisitor = new MyVisitor3(llfile, firstvisitor);
            
            root.accept(thirdvisitor, null);        //create Intermediate Code

            System.out.println("---Intermediate Code Generation Finished---");

            thirdvisitor.closeFile();

            firstvisitor.st.Clear();
            
            System.err.println("-----------------------------------------");


        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        catch(Error er){
            System.err.println(er.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}

}


