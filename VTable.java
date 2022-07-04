//PANAGIOTIS KONTOEIDIS
//1115201900266

import java.util.*;

public class VTable {
    
    SymbolTable st;

    LinkedHashMap<String, VTableInfo> Vtables;
    
    public VTable(SymbolTable s){
        Vtables = new LinkedHashMap<String, VTableInfo>();
        st = s;
        this.setVtables();
    }

    public void setVtables(){
        if(st.ClassCollector.keySet().isEmpty()){
            return;
        }
        for(String classname : st.ClassCollector.keySet()){
            this.setMethods(classname);
        }
        return;
    }

    public void setMethods(String classname){

        VTableInfo vt;

        if(st.getClassParent(classname).equals("null")){
            vt = new VTableInfo();
            if(st.getMethodnames(classname).equals(null)){
                return;
            }
            for( String method : st.getMethodnames(classname)){
                vt.setMethods(classname, method);
            }  

        }
        else{
            String parent = st.getClassParent(classname);
            vt = new VTableInfo(getVTInfo(parent));
            for(String method : st.getMethodnames(classname)){
                if(vt.getMethods().contains(method)){
                    vt.Replace(method, classname);
                }
                else    
                    vt.setMethods(classname, method);
            }
              
        }

        Vtables.put(classname, vt);

    }

    public VTableInfo getVTInfo(String classname){
        return Vtables.get(classname);
    }

    public Set<String> getMethods(String classname){
        return Vtables.get(classname).getMethods();
    }

    public void print(){
        Vtables.forEach((key, value)->System.out.println(key+"_Vtable \n"+value.print()));
    }

    public Integer getVtsize(String classname){
        return Vtables.get(classname).getSize();
    }

    public String getClass(String classname, String methodname){
        return Vtables.get(classname).getClass(methodname);
    }

    public Integer getMethodIndex(String classname, String methodname){
        return Vtables.get(classname).getIndex(methodname);
    }

}


class VTableInfo{

    SymbolTable st;

    private LinkedHashMap<String, String> Methods;

    public VTableInfo( ){
        Methods = new LinkedHashMap<>();
    }

    public VTableInfo(VTableInfo vt){
        Methods = new LinkedHashMap<>(vt.Methods);
    }

    public void setMethods(String classname, String methodname){
        Methods.put(methodname, classname);
    }

    public void Replace(String key, String value){
        Methods.replace(key, value);
    }

    public Set<String> getMethods(){
        return Methods.keySet();
    }

    public String print(){
        System.out.println(Methods);
        return "";
    }

    public Integer getSize(){
        return Methods.size();
    }

    public String getClass(String methodname){
        return Methods.get(methodname);
    }

    public Integer getIndex(String methodname){
        int index=0;
        for(String method : Methods.keySet()){
            if(method!=null){
                if(method.equals(methodname)){
                    return index;
                }
                else
                    index+=1;
            }
            }    
        return index;
    }
}
