//PANAGIOTIS KONTOEIDIS
//1115201900266
import java.util.*;

import javax.lang.model.util.ElementScanner6;


class SymbolTable{

    String classname;
    LinkedHashMap<String, ClassInfo> ClassCollector;; 
    ClassInfo classinfo;

    
    SymbolTable(){
        ClassCollector = new LinkedHashMap<String, ClassInfo>();
        classinfo = new ClassInfo();
    }
    //find type of a variable s
    public String searchType(String classname, String methodname, String s){
        if(s.equals("int")){
            return "int";
        }

        if(s.equals("boolean"))
            return "boolean";

        if(s.equals("boolean[]")){
            return "boolean[]";
        }

        if(s.equals("int[]")){
            return "int[]";
        }

        if(ClassCollector.containsKey(s)){
            return s;
        }

        if(getMethodVars(classname, methodname).containsKey(s)){
            return getMethodVars(classname, methodname).get(s);
        }

        if(getMethodParams(classname, methodname).containsKey(s)){
            return getMethodParams(classname, methodname).get(s);
        }
        
        if((ClassCollector.get(classname)).getVars().containsKey(s)){
            return (ClassCollector.get(classname)).getVars().get(s);
        }


        if(!getClassParent(classname).equals("null")){
            String parent = getClassParent(classname);
            if((ClassCollector.get(parent)).getVars().containsKey(s)){
                return (ClassCollector.get(parent)).getVars().get(s);
            }
            if(!getClassParent(parent).equals("null")){      //check for inheritance
                return helperParent(parent, s);
            }
        }
        
        return "null";
    }

    public String helperParent(String classname, String s){  //helper func to check for inheritance
        if((ClassCollector.get(classname)).getVars().containsKey(s)){
            return (ClassCollector.get(classname)).getVars().get(s);
        }
        if(!getClassParent(classname).equals("null")){
            String parent = getClassParent(classname);
            return helperParent(parent, s);
        }
        return "null";
    }

    public Boolean ParentForMethods(String classname, String s){  //helper func to check for inheritance
        if((ClassCollector.get(classname)).getMethodNames().contains(s)){
            return true;
        }
        if(!getClassParent(classname).equals("null")){
            String parent = getClassParent(classname);
            return ParentForMethods(parent, s);
        }
        else
            return false;
    }

    public String MessageSendParent(String classname, String s){  //helper func for Message Send 
                                                                    //returns the type of the inheritance
            if(!getClassParent(classname).equals("null")){
                String parent = getClassParent(classname);

                if(getMethodnames(parent).contains(s)){
                    return getMethodType(parent, s);
                }
                if(!getClassParent(parent).equals("null"))
                    return MessageSendParent(parent, s);
                else 
                    return "null";
                }
            return "null";
            }


    public String MessageSendParentMethods(String classname, String parentname){ //helper func for inheritance
        if(classname.equals("int")){                                             //between arguments in function calls
            return "null";
        }

        if(classname.equals("boolean"))
            return "null";

        if(classname.equals("boolean[]")){
            return "null";
        }

        if(classname.equals("int[]")){
            return "null";
        }

        if(!getClassParent(classname).equals("null")){

            String parent = getClassParent(classname);

            if(parent.equals(parentname)){
                return parentname;
            }
            if(!getClassParent(classname).equals("null")){
                return MessageSendParentMethods(parent, parentname);
            }
            else 
                return "null";
        }
        else 
            return "null";
    }

    public void setClassName(String classname){
        this.classname = classname;
        classinfo.setClassname(classname);
    }

    public Integer setVars(String var, String Type, Integer offset){
        return classinfo.setVars(var, Type, offset);
    }

    public void setParent(String par){
        classinfo.setParent(par);
    }

    public int setMethod(String methodname, String methodtype, Integer methodoffset){
        int a = classinfo.setMethod(methodname, methodtype, methodoffset);
        return a;

    }

    public void setMethodParams(String parName, String parType){
        classinfo.setMethodParams(parName, parType);
    }

    public void setMethodVars(String varname, String Vartype){
        classinfo.setMethodVars(varname, Vartype);
    }

    public void setVarOffsets(String varname, Integer offset){
        classinfo.setVarOffsets(varname, offset);
    }

    public void AddMethod(){
        classinfo.AddMethod();
    }

    public String getClassname(){
        return classname;
    }

    public LinkedHashMap<String, String> getClassVars(String Classname){
        return (this.ClassCollector.get(classname)).getVars();
        
    }

    public Set<String> getMethodnames(String classname){
        return (this.ClassCollector.get(classname)).getMethodNames();
    }

    public LinkedHashMap<String, String> getMethodVars(String classname, String methodname){
        return (this.ClassCollector.get(classname)).getMethodVars(methodname);
    }

    public LinkedHashMap<String, String> getMethodParams(String classname, String methodname){
        return (this.ClassCollector.get(classname)).getMethodParams(methodname);
    }

    public String getMethodType(String classname, String methodname){
        return (this.ClassCollector.get(classname).getMethodType(methodname));
    }

    public String getClassParent(String classname){
        return (this.ClassCollector.get(classname).getParent());
    }
    public LinkedHashMap<String, Integer> getVarOffsets(String classname){
        return (this.ClassCollector.get(classname).getVarOffsets());
    }

    public void AddToSymbolTable(){
        ClassInfo ci = new ClassInfo(classinfo);
        ClassCollector.put(new String(classname), ci);
        classinfo.clear();
    
    }

    public boolean isDefinedVar(String varname){        //returns true if varname is already defined
        return classinfo.isDefinedVar(varname);
    }

    public boolean isDefinedClass(String classname){ 
        return ClassCollector.containsKey(classname);
    }
    
    public boolean isDefinedParam(String paramname, String methodname){
        return classinfo.isDefinedParam(paramname, methodname);
    }

    public boolean isDefinedMethod(String methoname){
        return classinfo.isDefinedMethod(methoname);
    }

    public boolean isDefinedMethodVar(String methoname, String varname){
        return classinfo.isDefinedMethodVar(methoname, varname);
    }
    public void print(){
        ClassCollector.forEach((key, value) -> System.out.println( value.print()));
    }
    public void printoffsets(){
        ClassCollector.forEach((key, value)->System.out.println(value.printoffsets()));
    }
    public void Clear(){             //clears the whole symbol table
        classinfo.clear();
        ClassCollector.clear();
        classname = "null";
    }

    public Integer returnVarOffsets(String parentname){
        return ClassCollector.get(parentname).returnVarOffsets();
    }

    public Integer returnMethodOffsets(String parentname){
        return ClassCollector.get(parentname).returnMethodOffsets();
    }

    public Integer MethodsNum(String classname){
        return ClassCollector.get(classname).getMethodNames().size();
    }

    public Integer getLastVarOffset(String classname){
        return ClassCollector.get(classname).getLastVarOffset();
    }

    public Integer getLastVarSize(String classname){
        return ClassCollector.get(classname).getLastVarSize();
    }
}


class ClassInfo{


    private String Classname;
    private String parent;              
    private LinkedHashMap<String, String> varnames;     //map for variables
    private LinkedHashMap<String, MethodInfo> methods; //map for methods
    private MethodInfo tempmethod;       //temp map
    private LinkedHashMap<String, Integer> VarOffsets;
    private LinkedHashMap<String, Integer> MethodOffsets;
    private Integer LastVarOffset;
    private Integer LastMethodOffset;

    ClassInfo(){
        varnames = new LinkedHashMap<String, String>();       //map to save defined vars and their type
        methods = new LinkedHashMap<String, MethodInfo>();
        tempmethod = new MethodInfo();
        VarOffsets = new LinkedHashMap<String, Integer>();
        MethodOffsets = new LinkedHashMap<String, Integer>();
        Classname = null;
        parent = null;
        LastVarOffset=0;
        LastMethodOffset=0;

    }


    public ClassInfo(ClassInfo classinfo){

        this.Classname = new String(classinfo.getClassName());
        this.parent = new String(classinfo.getParent());
        this.varnames = new LinkedHashMap<>(classinfo.getVars());
        this.methods = new LinkedHashMap<String, MethodInfo>(classinfo.getMethods());
        this.VarOffsets = new LinkedHashMap<String, Integer>(classinfo.getVarOffsets());
        this.MethodOffsets = new LinkedHashMap<String, Integer>(classinfo.getMethodOffsets());
        this.LastVarOffset = classinfo.returnVarOffsets();
        this.LastMethodOffset = classinfo.returnMethodOffsets();

    }

    public void clear(){
        varnames.clear();
        Classname = null;
        parent = null;
        tempmethod.Clear();
        methods.clear();
        VarOffsets.clear();
        MethodOffsets.clear();
        LastMethodOffset=0;
        LastVarOffset=0;
    }

    public void AddMethod(){                //adds the method from temp map to methods map 
        MethodInfo mi = new MethodInfo(tempmethod);
        methods.put(tempmethod.getMethodname(), mi);  
        tempmethod.Clear();
    } 
    


    public void setClassname(String classname){             
        this.Classname = classname;
    }

    public void setParent(String par){                       //setter func for parent
        this.parent = par;
    }

    public int setVars(String varname, String Type, Integer varoffset){                   
        this.varnames.put(varname, Type);
        if(Type.equals("int")){
            setVarOffsets(varname, varoffset);
            varoffset+=4;
            LastVarOffset=varoffset;

            return varoffset;
        }
        if(Type.equals("boolean")){
            setVarOffsets(varname, varoffset);
            varoffset+=1;
            LastVarOffset=varoffset;
            return varoffset;

        }
        else{
            setVarOffsets(varname, varoffset);
            varoffset+=8;
            LastVarOffset= varoffset;
            return varoffset;
        }
        
    }
    
    public Integer setMethod(String methodname, String methodtype, Integer methodoffset){
        tempmethod.setMethod(methodname, methodtype);

        if(methodoffset!=-1){
            setMethodOffsets(methodname, methodoffset);
            methodoffset+=8;
            LastMethodOffset=methodoffset;
            return 8;
        }
        else{
            return 0;
        }
    }
    


    public void setMethodVars(String varname, String Vartype){
        tempmethod.setVars(varname, Vartype);
    }

    public void setMethodParams(String parName, String parType){
        tempmethod.setParams(parName, parType);
    }

    public void setVarOffsets(String varname, Integer offset){
        VarOffsets.put(varname, offset);
    }
    public void setMethodOffsets(String methodname, Integer offset){
        MethodOffsets.put(methodname, offset);
    }

    public String getParent(){                      //getter func for parent
        return this.parent;
    }

    public String getClassName(){
        return this.Classname;
    }
    
    public LinkedHashMap<String, String> getVars(){
        return this.varnames;                                  //returns the whole hash map which contains all the vars and their type
    }

    public LinkedHashMap<String, MethodInfo> getMethods(){
        return this.methods;
    }

    public Set<String> getMethodNames(){
        return this.methods.keySet();
    }

    public LinkedHashMap<String, String> getMethodVars(String methodname){
        return (this.methods.get(methodname)).getVars();
    }

    public LinkedHashMap<String, String> getMethodParams(String methodname){
        return (this.methods.get(methodname)).getParams();
    }

    public String getMethodType(String methoname){
        return this.methods.get(methoname).getMethodtype();
    }

    public String getMethodName(){
        return this.tempmethod.getMethodname();
    }
    public LinkedHashMap<String, Integer> getVarOffsets(){
        return this.VarOffsets;
    }
    public int getLastVarOffset(){
        int last=0;
        for(String var : VarOffsets.keySet()){
            last = VarOffsets.get(var);
        }
        return last;
    }

    public int getLastVarSize(){
        String last=null;
        for(String var : varnames.keySet()){
            last = varnames.get(var);
        }
        if(last==null){
            return 0;
        }
        else if(last.equals("int")){
            return 4;
        }
        else if(last.equals("boolean")){
            return 1;
        }
        else
            return 8;
    }


    public LinkedHashMap<String, Integer> getMethodOffsets(){
        return this.MethodOffsets;
    }
    public boolean isDefinedVar(String varname){
        return (varnames.keySet()).contains(varname);   //returns True if varname is already defined
    }

    public boolean isDefinedMethod(String methodname){
        return (methods.keySet()).contains(methodname);
    }

    public boolean isDefinedParam(String paramname, String methodname){
        return ((methods.get(methodname)).isDefinedParam(paramname));
    }

    public boolean isDefinedMethodVar(String methodname, String varname){
        return ((methods.get(methodname)).isDefinedVar(varname));
    }

    public String print(){
        System.out.println( "Class "+this.Classname+": extends "+this.parent+" + "+this.varnames);
        methods.forEach((key, value) -> System.out.println(value.print()));  
        return "";  
        }
    public String printoffsets(){
        System.out.println("-----Class: "+ Classname+"------\n");

        System.out.println("-------Variables-------\n");
        for(String key: VarOffsets.keySet()){
            System.out.println(Classname+"."+key+"="+VarOffsets.get(key));
        }
        System.out.println("-------Methods---------\n");
        for(String key: MethodOffsets.keySet()){
            System.out.println(Classname+"."+key+"="+MethodOffsets.get(key));
        }
        return "";  
    }

    public Integer returnVarOffsets(){
        return LastVarOffset;
    }

    public Integer returnMethodOffsets(){
        return LastMethodOffset;
    }

}


class MethodInfo{

    private String Methodname;
    private String MethodType;
    private LinkedHashMap<String, String> Varnames;   //map for variables
    private LinkedHashMap<String, String> Parameters;   //map for arguments


    public MethodInfo(){
        this.Varnames = new LinkedHashMap<String, String>();
        this.Parameters = new LinkedHashMap<String, String>();
    }

    public MethodInfo(MethodInfo methodinfo){
        this.Methodname = new String(methodinfo.getMethodname());
        this.MethodType = new String(methodinfo.getMethodtype());
        this.Varnames = new LinkedHashMap<>(methodinfo.getVars());
        this.Parameters = new LinkedHashMap<>(methodinfo.getParams());

    }

    public void Clear(){   
        Varnames.clear();
        Parameters.clear();
        MethodType=null;
        Methodname = null;
    }

    public boolean isDefinedVar(String varname){
        return (Varnames.keySet()).contains(varname);   //returns True if varname is already defined
    }

    public boolean isDefinedParam(String paramname){
        return (Parameters.keySet().contains(paramname));
    }


    public void setMethod(String methodname, String methodtype){
        this.Methodname = methodname;
        this.MethodType = methodtype;
    }

    public void setVars(String varname, String Vartype){
        if(this.Varnames.containsKey(varname))
            throw new Error("Variable "+varname+" at Method "+getMethodname()+ " already exists");
        if(this.Parameters.containsKey(varname))
            throw new Error("Variable "+varname+" at Method "+getMethodname()+ " is also a parameter");
        else    
            this.Varnames.put(varname, Vartype);

    }

    public void setParams(String parName, String parType){
        this.Parameters.put(parName, parType);
    }

    public String getMethodname(){
        return this.Methodname;
    }
    public String getMethodtype(){
        return this.MethodType;
    }

    public LinkedHashMap<String, String> getVars(){
        return this.Varnames;
    }

    public LinkedHashMap<String, String> getParams(){
        return this.Parameters;
    }

    public String print(){
        return "Method "+Methodname+Parameters+" has vars "+Varnames;
    }
}

