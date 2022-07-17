//PANAGIOTIS KONTOEIDIS
//1115201900266

import syntaxtree.*;
import visitor.*;

import java.lang.reflect.Method;
import java.util.*;

import javax.lang.model.util.ElementScanner6;





public class MyVisitor extends GJDepthFirst<String, Void>{
    
    
    SymbolTable st;
    int method_helper = 0; //==1 if in scope of method
    int varoffset =0;
    int methodoffset =0;
    MyVisitor(){
        st = new SymbolTable();
    }

    public void print(){  //print symboltable
        st.print();
    }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
       /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    @Override
    public String visit(Goal n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return null;
    }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    @Override
    public String visit(MainClass n,  Void argu) throws Exception {
        n.f0.accept(this, argu);
        String classname = n.f1.accept(this, argu);
        st.setClassName(classname);   
        st.setParent("null"); 
        st.setMethod("main", "void", -1);
        method_helper=1;
        n.f11.accept(this,argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        st.AddMethod();

        st.AddToSymbolTable();
        method_helper=0;
        return classname;
     }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, null);
        varoffset=0;
        methodoffset=0;
        String classname = n.f1.accept(this, null);
        if(st.isDefinedClass(classname)){
            throw new Exception("Class "+classname+" already exists");
        }
        else {
            st.setClassName(classname);   //set class name
        }
        st.setParent("null"); //parent does not exist

        String s = n.f3.accept(this, null);

        n.f4.accept(this, argu);
        st.AddToSymbolTable();  //add class to symbol table
        method_helper=0;

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        String classname = n.f1.accept(this, null);
        if(st.isDefinedClass(classname)){
            throw new Error("Class "+classname+" already exists");
        }
        else {
            st.setClassName(classname);  //set class name
        }
        String parent = n.f3.accept(this, argu);
        
        varoffset = st.returnVarOffsets(parent);  //get right var offset 
        
        methodoffset = st.returnMethodOffsets(parent);


        if(!st.isDefinedClass(parent)){  //check if class name already exists
            throw new Error("Parent Class "+parent+" does not exist");
        }

        else{
            st.setParent(parent);  //set parent
        }

        n.f5.accept(this, argu);

        n.f6.accept(this, argu);
        
        st.AddToSymbolTable();  //add class to symboltable
        
        method_helper=0;

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        String methodtype = n.f1.accept(this, null);
        String methodname = n.f2.accept(this, null);
        if(!st.isDefinedMethod(methodname)){
            if(!st.classinfo.getParent().equals("null")){
                if(st.ParentForMethods(st.classinfo.getParent(), methodname)){
                    methodoffset += st.setMethod(methodname, methodtype, -1);
                }
                else{
                    methodoffset += st.setMethod(methodname, methodtype, methodoffset);
                }
            }
            else{
                methodoffset += st.setMethod(methodname, methodtype, methodoffset);
            }
        }
        else
            throw new Error("Method "+methodname+" has been already defined");

        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "null";
        
        if(argumentList!="null"){
            String[] temp = argumentList.split(",");
            Set<String> tset= new HashSet<String>();
            for(String arg : temp){
                String params[] = arg.split("=");
                String partype = params[0].trim();
                String paramname = params[1].trim();
                if(tset.add(paramname)==false)  //check if parameter name already exists
                    throw new Error("Parameter "+paramname+" has been already defined");
                else
                    st.setMethodParams(paramname, partype);
            }
        }
        else
            st.setMethodParams(null, null);
        method_helper=1;
        n.f7.accept(this,null);   //get variables
        st.AddMethod();   //add method 

        return null;
    }


   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String vartype = n.f0.accept(this, null);
        String varname = n.f1.accept(this, null);
        if(method_helper==0){
            if(st.isDefinedVar(varname)) {
                throw new Error("Variable "+varname+ " already exists");
            }
            else{
                varoffset = st.setVars(varname, vartype, varoffset);  //set varname and vatype
            }
        }
        else{
            st.setMethodVars(varname, vartype);
        }

        return null;
     }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Void argu) throws Exception {
        String ret = n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Void argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Void argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }
        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + "=" + name;
    }

    @Override
    public String visit(BooleanArrayType n, Void argu) {
        return "boolean[]";
        }

    @Override
    public String visit(IntegerArrayType n, Void argu) {
        return "int[]";
    }
    @Override
    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }
    @Override
    public String visit(IntegerType n, Void argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Void argu) {        
        String s = n.f0.tokenImage;
        return s;
    }
}


