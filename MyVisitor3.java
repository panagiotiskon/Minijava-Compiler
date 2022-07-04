//PANAGIOTIS KONTOEIDIS
//1115201900266

import syntaxtree.*;
import visitor.*;

import java.util.*;


import java.io.*; 



public class MyVisitor3 extends GJDepthFirst<String, Void>{

Writer MyWriter;
SymbolTable st;
String currclass;
String currmethod;
VTable vt;
int method_helper=-1; //know when inside method

int method_reg_counter=0;


int integer_expression=-1; //use for IntegerLiteral
int boolean_expression=-1;  //use for |	TrueLiteral
                                 //    |	FalseLiteral
int this_expression=-1;
int is_reg=-1;   //when is_reg =1 program expects a register
int expr_reg_counter =0;  //holds the register to be return from an expression
String MessageSendItem=null;
String array_helper=null;

   public MyVisitor3(File llfile, MyVisitor myVisitor) throws Exception {
      
      st = myVisitor.st;

      vt = new VTable(st);   //create v_tables for all functions of all classes

      String filename = llfile.toString();
      filename = filename.split(".ll")[0];
      MyWriter = new FileWriter(llfile);

      for(String classname : st.ClassCollector.keySet()){
         int V_size=vt.getVtsize(classname);

         if(classname.equals(filename)){   //main func
               MyWriter.write("@."+filename+"_vtable = global [0 x i8*] []\n");
         }
         else{
            int counter =1;
            MyWriter.write("@."+classname+"_vtable = global ["+V_size+" x i8*][ ");
            if(V_size==0){
               MyWriter.write("[]\n");
            }
            else{
               //for every method in the Vtable
               for(String methodname : vt.getMethods(classname)){
                  MyWriter.write("i8* bitcast ( ");
                  MyWriter.write(llvmType(st.getMethodType(vt.getClass(classname, methodname), methodname)));
                  MyWriter.write(" (i8*");
                  if(st.getMethodParams(vt.getClass(classname, methodname), methodname).isEmpty()){
                     MyWriter.write(")");
                  }
                  else{                        
                     for(String arg : st.getMethodParams(vt.getClass(classname, methodname), methodname).values()){
                        if(arg!=null){
                           MyWriter.write(" ,"+llvmType(arg));
                        }
                     }
                  }
                  MyWriter.write(")* @"+vt.getClass(classname, methodname)+"."+methodname+" to i8*)");

                  if(counter< V_size){
                     MyWriter.write(",");
                  }
                     counter++;
                  }
               }
               MyWriter.write("]\n");
            }
         }
         
      definehelpers();   //define helper

   }

   public void closeFile()throws Exception{
      MyWriter.close();
   }

   public void definehelpers()throws Exception{


      MyWriter.write("\n\ndeclare i8* @calloc(i32, i32)\n");
      MyWriter.write("declare i32 @printf(i8*, ...)\n");
      MyWriter.write("declare void @exit(i32)\n\n");
      MyWriter.write("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n");
      MyWriter.write("@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
      MyWriter.write("define void @print_int(i32 %i) {\n");
      MyWriter.write("    %_str = bitcast [4 x i8]* @_cint to i8*\n");
      MyWriter.write("    call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
      MyWriter.write("    ret void\n");
      MyWriter.write("}\n\n");
      MyWriter.write("define void @throw_oob() {\n");
      MyWriter.write("    %_str = bitcast [15 x i8]* @_cOOB to i8*\n");
      MyWriter.write("    call i32 (i8*, ...) @printf(i8* %_str)\n");
      MyWriter.write("    call void @exit(i32 1)\n");
      MyWriter.write("    ret void\n");
      MyWriter.write("}\n\n");
   }


   public String llvmType(String type){
      if(type.equals("boolean"))
         return "i1";
      if(type.equals("int"))
         return "i32";
      if(type.equals("int[]"))
         return "i32*";
      else
         return "i8*";
   }

   /**
 * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
@Override public String visit(Goal n, Void argu) throws Exception {
   String _ret=null;
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return _ret;
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
@Override public String visit(MainClass n, Void argu) throws Exception {
   String _ret=null;

   MyWriter.write("\ndefine i32 @main(){\n");

   currclass = n.f1.accept(this, argu);

   n.f0.accept(this, argu);
   n.f2.accept(this, argu);
   n.f3.accept(this, argu);
   n.f4.accept(this, argu);
   n.f5.accept(this, argu);
   currmethod = "main";

   n.f6.accept(this, argu);
   method_helper=1;
   n.f11.accept(this, argu);
   n.f12.accept(this, argu);
   n.f13.accept(this, argu);
   n.f14.accept(this, argu);
   n.f15.accept(this, argu);
   n.f16.accept(this, argu);
   n.f17.accept(this, argu);
   method_helper=0;

   MyWriter.write("  ret i32 0\n}\n\n");

   return _ret;
}

/**
 * f0 -> ClassDeclaration()
*       | ClassExtendsDeclaration()
*/
@Override public String visit(TypeDeclaration n, Void argu) throws Exception {
   return n.f0.accept(this, argu);
}

/**
 * f0 -> "class"
* f1 -> Identifier()
* f2 -> "{"
* f3 -> ( VarDeclaration() )*
* f4 -> ( MethodDeclaration() )*
* f5 -> "}"
*/
@Override public String visit(ClassDeclaration n, Void argu) throws Exception {
   String _ret=null;

   currclass = n.f1.accept(this, argu);

   n.f2.accept(this, argu);
   n.f3.accept(this, argu);
   n.f4.accept(this, argu);
   n.f5.accept(this, argu);
   method_helper=0;

   return _ret;
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
@Override public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
   String _ret=null;
   n.f0.accept(this, argu);

   currclass = n.f1.accept(this, argu);

   n.f2.accept(this, argu);
   n.f3.accept(this, argu);
   n.f4.accept(this, argu);
   n.f5.accept(this, argu);
   n.f6.accept(this, argu);
   n.f7.accept(this, argu);
   method_helper=0;

   return _ret;
}

/**
 * f0 -> Type()
* f1 -> Identifier()
* f2 -> ";"
*/
@Override public String visit(VarDeclaration n, Void argu) throws Exception {
   String vartype = n.f0.accept(this, argu);
   String varname = n.f1.accept(this, argu);
   if(method_helper==1){
      MyWriter.write("  %"+varname+" = alloca "+llvmType(vartype)+"\n");
   }      
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
@Override public String visit(MethodDeclaration n, Void argu) throws Exception {
   
   method_helper=1;
   int pars_counter=1;
   
   String type = n.f1.accept(this, argu);
   currmethod = n.f2.accept(this, argu);

   MyWriter.write("define "+llvmType(type)+" @"+currclass+"."+currmethod+"(i8* %this");
   
   String argumentList = n.f4.accept(this, argu);
   if(argumentList!=null){
      MyWriter.write(", ");
   }
   if(argumentList!=null){
      String[] temp = argumentList.split(",");
      for (String arg : temp){
         String params[] = arg.split("=");
         String partype = params[0].trim();
         String paramname = params[1].trim();
         MyWriter.write(llvmType(partype)+" %."+paramname);
         if(pars_counter<temp.length){
            MyWriter.write(", ");
         }
         pars_counter++;
      }
      MyWriter.write("){\n");

      for (String arg : temp){
         String params[] = arg.split("=");
         String partype = params[0].trim();
         String paramname = params[1].trim();
         MyWriter.write("  %"+paramname+" = alloca "+llvmType(partype)+"\n");
         MyWriter.write("  store "+llvmType(partype)+" %."+paramname+", "+llvmType(partype)+"* %"+paramname+"\n");
      }
   }
   else{
      MyWriter.write("){\n");
   }


   n.f5.accept(this, argu);
   n.f6.accept(this, argu);
   n.f7.accept(this, argu);
   n.f8.accept(this, argu);
   n.f9.accept(this, argu);


   String s = n.f10.accept(this, argu);

   int counter=method_reg_counter;
   if(s==null){
      return null;
   }
   if(s.equals("int")){
      MyWriter.write("  ret i32 "+integer_expression+"\n\n");
   }
   if(s.equals("boolean")){
      MyWriter.write("  ret i32 "+ boolean_expression+"\n\n");
   }
   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      String llvm_type = llvmType(type);
      MyWriter.write("  %_"+counter+" = load "+llvm_type+","+llvm_type+"* %"+s+"\n");

      MyWriter.write("  ret "+ llvm_type+" %_"+counter+"\n\n");
   }
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");

      MyWriter.write("  ret "+llvm_type+" %_"+counter+"\n\n");
   }
   else if (is_reg==1 & expr_reg_counter!=-1){
      String llvm_type = llvmType(type);
      MyWriter.write("  ret "+llvm_type+" %_"+expr_reg_counter+"\n\n");
   }

   method_helper=0;
   method_reg_counter=0;
   is_reg=-1;

   MyWriter.write("}\n\n");

   return null;
}

/**
 * f0 -> FormalParameter()
* f1 -> FormalParameterTail()
*/
@Override public String visit(FormalParameterList n, Void argu) throws Exception {
   String ret= n.f0.accept(this, argu);
   if(n.f1!=null)
      ret+=n.f1.accept(this, argu);
   return ret;
}

/**
 * f0 -> Type()
* f1 -> Identifier()
*/
@Override public String visit(FormalParameter n, Void argu) throws Exception {
   String type = n.f0.accept(this, null);
   String name = n.f1.accept(this, null);
   return type + "=" + name;
}

/**
 * f0 -> ( FormalParameterTerm() )*
*/
@Override public String visit(FormalParameterTail n, Void argu) throws Exception {
   String ret = "";
   for ( Node node: n.f0.nodes) {
      ret += ", " + node.accept(this, null);
   }
   return ret;
   }

/**
 * f0 -> ","
* f1 -> FormalParameter()
*/
@Override public String visit(FormalParameterTerm n, Void argu) throws Exception {
   return n.f1.accept(this, argu);
}

/**
 * f0 -> ArrayType()
*       | BooleanType()
*       | IntegerType()
*       | Identifier()
*/
@Override public String visit(Type n, Void argu) throws Exception {
   return n.f0.accept(this, argu);
}

/**
 * f0 -> BooleanArrayType()
*       | IntegerArrayType()
*/
@Override public String visit(ArrayType n, Void argu) throws Exception {
   return n.f0.accept(this, argu);
}

/**
* f0 -> "boolean"
* f1 -> "["
* f2 -> "]"
*/
@Override public String visit(BooleanArrayType n, Void argu) throws Exception {
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return "boolean[]";
}

/**
* f0 -> "int"
* f1 -> "["
* f2 -> "]"
*/
@Override public String visit(IntegerArrayType n, Void argu) throws Exception {
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return "int[]";
}

/**
* f0 -> "boolean"
*/
@Override public String visit(BooleanType n, Void argu) throws Exception {
   return "boolean";
}

/**
* f0 -> "int"
*/
@Override public String visit(IntegerType n, Void argu) throws Exception {
   return "int";
}

/**
* f0 -> Block()
*       | AssignmentStatement()
*       | ArrayAssignmentStatement()
*       | IfStatement()
*       | WhileStatement()
*       | PrintStatement()
*/
@Override public String visit(Statement n, Void argu) throws Exception {
   String s = n.f0.accept(this, argu);
   method_reg_counter++;
   return s;
}

/**
* f0 -> "{"
* f1 -> ( Statement() )*
* f2 -> "}"
*/
@Override public String visit(Block n, Void argu) throws Exception {
   String _ret=null;
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return _ret;
}

/**
* f0 -> Identifier()
* f1 -> "="
* f2 -> Expression()
* f3 -> ";"
*/
@Override public String visit(AssignmentStatement n, Void argu) throws Exception {

   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;

   int counter = method_reg_counter;

   String id = n.f0.accept(this, argu);
   
   String llvm_type = llvmType(st.searchType(currclass, currmethod, id));

   String expression = n.f2.accept(this, argu);


   if(expression==null){
      return null;
   }


   //if ID is in the method arguments

   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(id)){

      if(is_reg==0){    //if is_reg = 0 then booleans and numbers are assigned
         
         if(boolean_expression==1){
            MyWriter.write("  store i1 1, i1* %"+id+"\n\n");
         }
         else if(boolean_expression==0){
            MyWriter.write("  store i1 0, i1* %"+id+"\n\n");
         }
         else if(integer_expression!=-1){
            MyWriter.write("  store i32 "+integer_expression+", i32* %"+id+"\n\n");
         }
         else if(this_expression==1){
            MyWriter.write("  store i8* %this, i8** %"+id+"\n\n");
         }

      }
      else if(is_reg==1){   //if 

         if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(expression) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(expression)){
            MyWriter.write("  %_"+counter +" = load "+llvm_type+" , "+llvm_type+" * "+"%"+expression+"\n");
            MyWriter.write("  store "+llvm_type+" %_"+counter+", "+llvm_type+"* %"+id+"\n\n");
         
         } 

         if(st.ClassCollector.get(currclass).getVars().containsKey(expression)){

            int offset2 = st.getVarOffsets(currclass).get(expression) + 8;

            MyWriter.write("  %_"+method_reg_counter+" = getelementptr i8, i8* %this, i32 "+ offset2+ "\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %"+(counter-1)+"\n");

            MyWriter.write("  store "+llvm_type+" %_"+(counter)+", "+llvm_type+"* %"+id+"\n\n");
         }

         if(expr_reg_counter!=-1){
            MyWriter.write("  store "+llvm_type+" %_"+expr_reg_counter+", "+llvm_type+"* %"+id+"\n\n");
         }

      }
      boolean_expression=-1;
      integer_expression=-1;
      this_expression=-1;

   }

   //check if id in method variables

   if(st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(id)){

      if(is_reg==0){    //if is_reg = 0 then booleans and numbers are assigned

         if(boolean_expression==1){
            MyWriter.write("  store i1 1, i1* %"+id+"\n\n");
         }
         else if(boolean_expression==0){
            MyWriter.write("  store i1 0, i1* %"+id+"\n\n");
         }
         else if(integer_expression!=-1){
            MyWriter.write("  store i32 "+integer_expression+", i32* %"+id+"\n\n");
         }
         else if(this_expression==1){
            MyWriter.write("  store i8* %this, i8** %"+id+"\n\n");
         }

      }
      else if(is_reg==1){   //if 
         if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(expression) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(expression)){
            MyWriter.write("  %_"+counter +" = load "+llvm_type+" , "+llvm_type+" * "+"%"+expression+"\n");
            MyWriter.write("  store "+llvm_type+" %_"+counter+", "+llvm_type+"* %"+id+"\n\n");
         
         } 
         else if(st.ClassCollector.get(currclass).getVars().containsKey(expression)){

            int offset2 = st.getVarOffsets(currclass).get(expression) + 8;

            MyWriter.write("  %_"+method_reg_counter+" = getelementptr i8, i8* %this, i32 "+ offset2+ "\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %_"+(counter-1)+"\n");

            MyWriter.write("  store "+llvm_type+" %_"+(counter)+", "+llvm_type+"* %"+id+"\n\n");
         }
         
         else if(expr_reg_counter!=-1){
            MyWriter.write("  store "+llvm_type+" %_"+expr_reg_counter+", "+llvm_type+"* %"+id+"\n\n");
         }
      }
      boolean_expression=-1;
      integer_expression=-1;
      this_expression=-1;
   }

         //check if id is in class vars

   else if(st.ClassCollector.get(currclass).getVars().containsKey(id)){

      if(is_reg==0){    //if is_reg = 0 then booleans and numbers are assigned

         int offset = st.getVarOffsets(currclass).get(id) + 8;

         counter = ++method_reg_counter;
         MyWriter.write("  %_"+method_reg_counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         if(boolean_expression==1){
            MyWriter.write("  store i1 1, i1* %_"+counter+"\n\n");
         }
         else if(boolean_expression==0){
            MyWriter.write("  store i1 0, i1* %_"+counter+"\n\n");
         }
         else if(integer_expression!=-1){
            MyWriter.write("  store i32 "+integer_expression+", i32* %_"+counter+"\n\n");
         }
         else if(this_expression==1){
            MyWriter.write("  store i8* %this, i8** %"+counter+"\n\n");
         }
      }

      else if(is_reg==1){   
         counter = method_reg_counter;
         int offset = st.getVarOffsets(currclass).get(id) + 8;

         if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(expression) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(expression)){
            MyWriter.write("  %_"+method_reg_counter+" = load "+llvm_type+", "+llvm_type+"* %"+expression+"\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
            MyWriter.write("  store "+llvm_type+" %_"+(counter-2)+", "+llvm_type+"* %_"+counter+"\n\n");
         } 
         else if(expr_reg_counter!=-1){

            MyWriter.write("  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
            counter = ++method_reg_counter;
            MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
            MyWriter.write("  store "+llvm_type+" %_"+expr_reg_counter+", "+llvm_type+"* %_"+counter+"\n\n");
         }
      }

   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;
   }

   return null;
}

/**
* f0 -> Identifier()
* f1 -> "["
* f2 -> Expression()
* f3 -> "]"
* f4 -> "="
* f5 -> Expression()
* f6 -> ";"
*/
@Override public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;
   int counter=method_reg_counter;      
   int id_counter=-1;
   int exp1=-1;
   int exp2=-1;


   String id = n.f0.accept(this, argu);

   String llvm_type = llvmType(st.searchType(currclass, currmethod, id));

   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(id) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(id)){
      MyWriter.write("  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %"+id+"\n");
      id_counter= counter;
      counter=++method_reg_counter;
   }
   else if (st.ClassCollector.get(currclass).getVars().containsKey(id)){
      int offset = st.getVarOffsets(currclass).get(id) + 8;
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %_"+(counter-1)+"\n");
      id_counter= counter;
      counter=++method_reg_counter;
   } 

   String s1 = n.f2.accept(this, argu);

   if(s1==null){
      return null;
   }

   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
      MyWriter.write("  %_"+counter+" = load i32, i32* %"+s1+"\n");
      exp1=counter;
      counter=++method_reg_counter;
   } 
   
   else if(st.ClassCollector.get(currclass).getVars().containsKey(s1)){
      int offset = st.getVarOffsets(currclass).get(s1) + 8;
      String llvm_type1 = llvmType(st.searchType(currclass, currmethod, s1));
      MyWriter.write("  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type1+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
      expr_reg_counter=counter;
      exp1=counter;
      counter=++method_reg_counter;

   }
   else if(s1.equals("int") && integer_expression!=-1){
      exp1 = integer_expression;
   }
   else if(expr_reg_counter!=-1){
      exp1 = expr_reg_counter;
      counter=++method_reg_counter;
   }
   

   MyWriter.write("  %_"+counter+" = load i32, i32* %_"+ id_counter+"\n");
   counter=++method_reg_counter;

   if(s1.equals("int")){
      MyWriter.write("  %_"+counter+" = icmp ult i32 "+exp1+", %_"+(counter-1)+"\n");
   }
   else{   
      MyWriter.write("  %_"+counter+" = icmp ult i32 %_"+exp1+", %_"+(counter-1)+"\n");
   }
   int oob_counter=counter+1;

   MyWriter.write("  br i1 %_"+counter+", label %oob"+oob_counter+", label %oob"+(oob_counter+1)+"\n\n");
   counter=++method_reg_counter;
   counter=++method_reg_counter;
   counter=++method_reg_counter;
   MyWriter.write("oob"+(oob_counter)+":\n");
   if(s1.equals("int")){
      MyWriter.write("  %_"+counter+" = add i32 "+exp1+", 1\n");
   }
   else{   
      MyWriter.write("  %_"+counter+" = add i32 %_"+exp1+", 1\n");
   }
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = getelementptr i32, i32* %_"+id_counter+" , i32 %_"+(counter-1)+"\n");
   id_counter=counter;
   counter=++method_reg_counter;


   String s2 = n.f5.accept(this, argu);


   if(s2==null){
      return null;
   }
   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
      MyWriter.write("  %_"+counter+" = load i32, i32* %"+s2+"\n");
      exp2 = counter;
      counter=++method_reg_counter;
   } 
   
   else if(st.ClassCollector.get(currclass).getVars().containsKey(s2)){
      int offset = st.getVarOffsets(currclass).get(s2) + 8;
      String llvm_type2 = llvmType(st.searchType(currclass, currmethod, s2));
      MyWriter.write("  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type2+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
      exp2 = counter;
      counter=++method_reg_counter;
   }
   else if(s2.equals("int") && integer_expression!=-1){
      exp2 = integer_expression;
   }
   else if(expr_reg_counter!=-1){
      exp2 = expr_reg_counter;
      counter=++method_reg_counter;
   }


   if(s1.equals("int")){
      MyWriter.write("  store i32 "+exp2+" , i32* %_"+id_counter+"\n");
   }
   else{   
      MyWriter.write("  store i32 %_"+exp2+" , i32* %_"+id_counter+"\n");
   }
   
   MyWriter.write("  br label %oob"+(oob_counter+2)+"\n\n");

   MyWriter.write("oob"+(oob_counter+1)+":\n");
   MyWriter.write("  call void @throw_oob()\n");
   MyWriter.write("  br label %oob"+(oob_counter+2)+"\n");
   MyWriter.write("oob"+(oob_counter+2)+":\n\n");

   boolean_expression=-1;
   integer_expression=-1;
   
   
   return null;
}

/**
* f0 -> "if"
* f1 -> "("
* f2 -> Expression()
* f3 -> ")"
* f4 -> Statement()
* f5 -> "else"
* f6 -> Statement()
*/
@Override public String visit(IfStatement n, Void argu) throws Exception {
   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;

   int counter=method_reg_counter;

   String s = n.f2.accept(this, argu);
   if(s==null){
      return null;
   }
   if(s.equals("boolean")){
      MyWriter.write("  %_"+counter+" = add i32 %_"+counter+", "+boolean_expression+"\n");
      MyWriter.write("  br i1 %_"+counter+", label %if"+(counter+1)+", label %if"+(counter+2)+"\n");
   }
   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      MyWriter.write("  %_"+counter+" = load i32, i32* %"+s+"\n");
   
   }
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");

      MyWriter.write("  call void (i32) @print_int(i32 %_"+counter+")\n");
   }
   if(is_reg==1){
      if(expr_reg_counter!=-1){
         counter=--method_reg_counter;
         MyWriter.write("  br i1 %_"+expr_reg_counter+", label %if"+(counter+1)+", label %if"+(counter+2)+"\n");
      }
   }

   counter=++method_reg_counter;
   counter=++method_reg_counter;
   counter=++method_reg_counter;

   MyWriter.write("if"+(counter-2)+(":\n"));
   n.f4.accept(this, argu);
   MyWriter.write("  br label %if"+(counter)+"\n");
   MyWriter.write("if"+(counter-1)+":\n");
   n.f6.accept(this, argu);
   MyWriter.write("  br label %if"+(counter)+"\n");
   MyWriter.write("if"+(counter)+":\n");

   return null;
}

/**
* f0 -> "while"
* f1 -> "("
* f2 -> Expression()
* f3 -> ")"
* f4 -> Statement()
*/
@Override public String visit(WhileStatement n, Void argu) throws Exception {
   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;

   int counter = method_reg_counter;
   int loop_counter = counter;
   MyWriter.write("  br label %loop"+loop_counter+" \n\n");

   MyWriter.write("loop"+loop_counter+":\n");

   String s = n.f2.accept(this, argu);
   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      MyWriter.write("  %_"+counter+" = load "+llvm_type +", "+llvm_type+"* %"+s+"\n");
      MyWriter.write("  br i1 %_"+counter+", label %loop"+(loop_counter+1)+", label %loop"+(loop_counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

   }
   else if(st.ClassCollector.get(currclass).getVars().containsKey(s)){
      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      MyWriter.write("  %_"+counter+" = load "+llvm_type +", "+llvm_type+"* %"+s+"\n");
      MyWriter.write("  br i1 %_"+counter+", label %loop"+(loop_counter+1)+", label %loop"+(loop_counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

   }
   else if(is_reg==1 && expr_reg_counter!=-1){
      MyWriter.write("  br i1 %_"+expr_reg_counter+", label %loop"+(loop_counter+1)+", label %loop"+(loop_counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

   }

   MyWriter.write("loop"+(loop_counter+1)+":\n");
   n.f4.accept(this, argu);

   MyWriter.write("  br label %loop"+(loop_counter)+"\n\n");

   MyWriter.write("loop"+(loop_counter+2)+":\n\n");

   return null;
}

/**
* f0 -> "System.out.println"
* f1 -> "("
* f2 -> Expression()
* f3 -> ")"
* f4 -> ";"
*/
@Override public String visit(PrintStatement n, Void argu) throws Exception {
   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;
   int counter=method_reg_counter;

   String s = n.f2.accept(this, argu);
   if(s==null){
      return null;
   }

   if(s.equals("int")){
      MyWriter.write("  call void (i32) @print_int(i32 "+integer_expression+")\n");
   }
   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      MyWriter.write("  %_"+counter+" = load i32, i32* %"+s+"\n");
      MyWriter.write("  call void (i32) @print_int(i32 %_"+counter+")\n");
   }
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");

      MyWriter.write("  call void (i32) @print_int(i32 %_"+counter+")\n");
   }
   else if (is_reg!=-1){
      MyWriter.write("  call void (i32) @print_int(i32 %_"+expr_reg_counter+")\n");
   }

   boolean_expression=-1;
   integer_expression=-1;
   is_reg=-1;
   this_expression=-1;
   expr_reg_counter=0;
   
   return null;
}

/**
* f0 -> AndExpression()
*       | CompareExpression()
*       | PlusExpression()
*       | MinusExpression()
*       | TimesExpression()
*       | ArrayLookup()
*       | ArrayLength()
*       | MessageSend()
*       | Clause()
*/
@Override public String visit(Expression n, Void argu) throws Exception {

   String s = n.f0.accept(this, argu);

   if(s==null){
      return null;
   }
   else if(s.equals("boolean")){
      if(boolean_expression==1){
         is_reg=0;
         return s;
      }
      else if(boolean_expression==0){
         is_reg=0;
         return s;
      }
   }
   else if(s.equals("int")){
      if(integer_expression!=-1){
         is_reg=0;
      }
   }
   else if(st.ClassCollector.keySet().contains(s)){
      is_reg=0;
   }
   else if(s.equals("this")){
      is_reg=0;
   }
   else{
      is_reg=1;
   }

   return s;
}

/**
* f0 -> Clause()
* f1 -> "&&"
* f2 -> Clause()
*/
@Override public String visit(AndExpression n, Void argu) throws Exception {
   String _ret=null;
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return _ret;
}

/**
* f0 -> PrimaryExpression()
* f1 -> "<"
* f2 -> PrimaryExpression()
*/
@Override public String visit(CompareExpression n, Void argu) throws Exception {
   String s1= n.f0.accept(this, argu);

   int int1=-1;
   int int2=-1;
   int reg1=-1;
   int reg2=-1;

   int counter = method_reg_counter;

   if(s1.equals("int")){
      int1 = integer_expression;
   }

   if(s1==null||s1.equals("int[]")||s1.equals("boolean[]")||s1.equals("new")){
      return null;
   }

   if(is_reg==1 && expr_reg_counter!=-1){
      reg1 = expr_reg_counter;
   }

   String s2 =n.f2.accept(this, argu);

   if(s2.equals("int")){
      int2 = integer_expression;
   }
   if(s2 == null||s2.equals("int[]")||s2.equals("boolean[]")||s2.equals("new")){
      return null;
   }

   //if both primary expressions are simple ints

   if(int1!=-1 && int2!=-1){
      MyWriter.write("  %_"+counter+" = icmp slt "+int1+" "+int2+"\n");
      expr_reg_counter=counter;
   }

   // if s1 is an id and s2 is int 

   else if(int1==-1 && int2!=-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = icmp slt i32 %_"+(counter-1)+", "+int2+"\n");

      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = icmp slt i32 %_"+(counter-1)+", "+int2+"\n");
      }

      //if a register is returned to id1

      else if(is_reg==1){
         if(reg1!=-1){
            MyWriter.write("  %_"+counter+" = icmp slt i32 %_"+reg1+", "+int2+"\n\n");
         }
      }
   }

   else if(int1!=-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
               
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = icmp slt i32 "+int1+", %_"+(counter-1)+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = icmp slt i32 "+int1 +", %_"+(counter-1)+"\n");
      }

      if(is_reg==1){         
         if(reg2!=-1){
         MyWriter.write("  %_"+counter+" = icmp slt i32 "+int1+", %_"+reg2+"\n\n");
         }
      }
   }

   //if none of s1 and s2 is int 

   else if(int1==-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %"+s1+"\n");
         
         reg1=counter;
      }

      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %"+(counter-1)+"\n");
         
         reg1=counter;
      }


      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         counter=++method_reg_counter;            
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));

         MyWriter.write("\n  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %"+s2+"\n");
         reg2=counter;
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load "+llvm_type+", "+llvm_type+"* %_"+(counter-1)+"\n");
         reg2=counter;
      }
      else if(is_reg==1 && expr_reg_counter!=-1){
         reg2 = expr_reg_counter;
      }
      counter=++method_reg_counter;
      MyWriter.write("\n  %_"+counter+" = icmp slt i32 %_"+reg1 +", %_"+reg2+"\n");

   }

   expr_reg_counter=counter;
   counter=++method_reg_counter;

   return "comp";
}


/**
* f0 -> PrimaryExpression()
* f1 -> "+"
* f2 -> PrimaryExpression()
*/
@Override public String visit(PlusExpression n, Void argu) throws Exception {
   
   String s1= n.f0.accept(this, argu);

   int int1=-1;
   int int2=-1;
   int reg1=-1;
   int reg2=-1;

   int counter = method_reg_counter;

   if(s1.equals("int")){
      int1 = integer_expression;
   }
   if(s1==null||s1.equals("int[]")||s1.equals("boolean[]")||s1.equals("new")){
      return null;
   }

   if(is_reg==1 && expr_reg_counter!=-1){
      reg1 = expr_reg_counter;
   }

   String s2 =n.f2.accept(this, argu);

   if(s2.equals("int")){
      int2 = integer_expression;
   }
   if(s2 == null||s2.equals("int[]")||s2.equals("boolean[]")||s2.equals("new")){
      return null;
   }


   //if both primary expressions are simple ints

   if(int1!=-1 && int2!=-1){
      MyWriter.write("  %_"+counter+" = add i32 "+int1+" "+int2+"\n");
   }

   // if s1 is an id and s2 is int 

   else if(int1==-1 && int2!=-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = add i32 %_"+(counter-1)+", "+int2+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = add i32 %_"+(counter-1)+", "+int2+"\n");
      }

      //if a register is returned to id1

      if(is_reg==1){
         if(reg1!=-1){
            MyWriter.write("  %_"+counter+" = add i32 %_"+reg1+", "+int2+"\n\n");
         }
      }


   }
   else if(int1!=-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = add i32 "+int1+", %_"+(counter-1)+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = add i32 "+int1 +", %_"+(counter-1)+"\n");
      }

      if(is_reg==1){         
         if(reg2!=-1){
         MyWriter.write("  %_"+counter+" = add i32 "+int1+", %_"+reg2+"\n\n");
         }
      }

   }

   //if none of s1 and s2 is int 

   else if(int1==-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
         
         reg1=counter;
      }

      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
         
         reg1=counter;
      }

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
         reg2=counter;
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
         reg2=counter;
      }
      else if(is_reg==1 && expr_reg_counter!=-1){
         reg2 = expr_reg_counter;
      }
      counter=++method_reg_counter;
      MyWriter.write("\n %_"+counter+" = add i32 %_"+reg1 +", %_"+reg2+"\n");

   }

   expr_reg_counter=counter;
   return "plus";
}

/**
* f0 -> PrimaryExpression()
* f1 -> "-"
* f2 -> PrimaryExpression()
*/
@Override public String visit(MinusExpression n, Void argu) throws Exception {
   
   String s1= n.f0.accept(this, argu);

   int int1=-1;
   int int2=-1;
   int reg1=-1;
   int reg2=-1;

   int counter = method_reg_counter;

   if(s1.equals("int")){
      int1 = integer_expression;
   }
   if(s1==null||s1.equals("int[]")||s1.equals("boolean[]")||s1.equals("new")){
      return null;
   }

   if(is_reg==1 && expr_reg_counter!=-1){
      reg1 = expr_reg_counter;
   }

   String s2 =n.f2.accept(this, argu);

   if(s2.equals("int")){
      int2 = integer_expression;
   }
   if(s2 == null||s2.equals("int[]")||s2.equals("boolean[]")||s2.equals("new")){
      return null;
   }

   if(is_reg==1){
      if(expr_reg_counter!=-1){reg2 = expr_reg_counter;}
   }



   //if both primary expressions are simple ints

   if(int1!=-1 && int2!=-1){
      MyWriter.write("  %_"+counter+" = sub i32 "+int1+", "+int2+"\n");
   }

   // if s1 is an id and s2 is int 

   else if(int1==-1 && int2!=-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = sub i32 %_"+(counter-1)+", "+int2+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = sub i32 %_"+(counter-1)+", "+int2+"\n");
      }

      //if a register is returned to id1

      else if(is_reg==1){
         if(reg1!=-1){
            MyWriter.write("  %_"+counter+" = sub i32 %_"+reg1+", "+int2+"\n\n");
         }
      }


   }
   else if(int1!=-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = sub i32 "+int1+", %_"+(counter-1)+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = sub i32 "+int1 +", %_"+(counter-1)+"\n");
      }

      else if(is_reg==1){         
         if(reg2!=-1){
         MyWriter.write("  %_"+counter+" = sub i32 "+int1+", %_"+reg2+"\n\n");
         }
      }

   }

   //if none of s1 and s2 is int 

   else if(int1==-1 && int2==-1){

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         String llvm_type=llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = load "+llvm_type+","+llvm_type+"* %"+s1+"\n");
         
         reg1=counter;
      }

      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %"+(counter-1)+"\n");
         
         reg1=counter;
      }

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = load i1, i1* %"+s2+"\n");
         reg2=counter;
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
         reg2=counter;
      }
      counter=++method_reg_counter;
      MyWriter.write("\n %_"+counter+" = sub i32 %_"+reg1 +", %_"+reg2+"\n");


   }
   is_reg=1;
   expr_reg_counter=counter;
   
   return "minus";
}

/**
* f0 -> PrimaryExpression()
* f1 -> "*"
* f2 -> PrimaryExpression()
*/

@Override public String visit(TimesExpression n, Void argu) throws Exception {
   
   String s1= n.f0.accept(this, argu);

   int int1=-1;
   int int2=-1;
   int reg1=-1;
   int reg2=-1;

   int counter=method_reg_counter;

   if(s1==null||s1.equals("int[]")||s1.equals("boolean[]")||s1.equals("new")){
      return null;
   }
   
   if(s1.equals("int")){
      int1 = integer_expression;
   }


   if(expr_reg_counter!=-1){
      reg1 = expr_reg_counter;
   }

   String s2 =n.f2.accept(this, argu);

   if(s2 == null||s2.equals("int[]")||s2.equals("boolean[]")||s2.equals("new")){
      return null;
   }
   
   if(s2.equals("int")){
      int2 = integer_expression;
   }


   if(is_reg==1){
      if(expr_reg_counter!=-1){reg2 = expr_reg_counter;}
   }



   //if both primary expressions are simple ints

   if(int1!=-1 && int2!=-1){
      counter= ++method_reg_counter;

      MyWriter.write("  %_"+counter+" = mul i32 "+int1+" "+int2+"\n");
   }

   // if s1 is an id and s2 is int 

   else if(int1==-1 && int2!=-1){
      counter= ++method_reg_counter;

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = mul i32 %_"+(counter-1)+", "+int2+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = mul i32 %_"+(counter-1)+", "+int2+"\n");
      }

      //if a register is returned to id1

      if(is_reg==1){
         if(reg1!=-1){
            MyWriter.write("  %_"+counter+" = mul i32 %_"+reg1+", "+int2+"\n\n");
         }
      }


   }
   else if(int1!=-1 && int2==-1){
      counter= ++method_reg_counter;

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = mul i32 "+int1+", %_"+(counter-1)+"\n");
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %"+(counter-1)+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = mul i32 "+int1 +", %_"+(counter-1)+"\n");
      }

      if(is_reg==1){         
         if(reg2!=-1){
         MyWriter.write("  %_"+counter+" = mul i32 "+int1+", %_"+reg2+"\n\n");
         }
      }

   }

   //if none of s1 and s2 is int 

   else if(int1==-1 && int2==-1){ 
      counter= ++method_reg_counter;
      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = load "+llvm_type+", "+ llvm_type+"* %"+s1+"\n");
         reg1=counter;
      }

      else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
         int offset = st.getVarOffsets(currclass).get(s1) + 8;
         String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %"+(counter-1)+"\n");
         
         reg1=counter;
      }

      if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = load i1, i1* %"+s2+"\n");
         reg2=counter;
      }
      else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
         int offset = st.getVarOffsets(currclass).get(s2) + 8;

         String llvm_type = llvmType(st.searchType(currclass, currmethod, s2));
         
         counter=++method_reg_counter;
         MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i1, i1* %"+(counter-1)+"\n");
         reg2=counter;
      }
      counter=++method_reg_counter;
      MyWriter.write("\n %_"+counter+" = mul i32 %_"+reg1 +", %_"+reg2+"\n");


   }

   expr_reg_counter=counter;
   return "plus";
}

/**
* f0 -> PrimaryExpression()
* f1 -> "["
* f2 -> PrimaryExpression()
* f3 -> "]"
*/
@Override public String visit(ArrayLookup n, Void argu) throws Exception {
   int reg1 = -1;
   int reg2 = -1;
   int array_name=-1;
   int index =-1;
   int size=-1;
   int counter = ++method_reg_counter;

   String s1 = n.f0.accept(this, argu);

   if(s1==null){
      return null;
   }
   if(is_reg==1 && expr_reg_counter!=-1){
      reg1 = expr_reg_counter;
   }
   String s2 = n.f2.accept(this, argu);

   if(s2==null){
      return null;
   }
   if(s2.equals("int")){
      size=integer_expression;
   }

   if(is_reg==1 && expr_reg_counter!=-1){
      reg2 = expr_reg_counter;
   }

      //get array name
   
      //if it is in parameters of method vars
   
   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s1) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s1)){
      MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s1+"\n");
      array_name = counter;
      counter=++method_reg_counter;
   }

   //if it is in class vars
   
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s1)){
      int offset = st.getVarOffsets(currclass).get(s1) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32*, i32** %_"+(counter-1)+"\n");
      array_name = counter;
      counter=++method_reg_counter;
   }


      //get size

   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s2) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s2)){
      MyWriter.write("\n  %_"+counter+" = load i32, i32* %"+s2+"\n");
      index = counter;
   }
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s2)){
      int offset = st.getVarOffsets(currclass).get(s2) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s1));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32*, i32** %_"+(counter-1)+"\n");
      index = counter;
   }


   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = load i32, i32* %_"+array_name+"\n");
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = icmp ult i32 %_"+index+", %_"+(counter-1) +"\n");
   int oob = counter;
   MyWriter.write("  br i1 %_"+counter+", label %oob"+(oob+1)+", label %oob"+(oob+2)+"\n\n");

   MyWriter.write("oob"+(oob+1)+":\n");


   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = add i32 %_"+index+", 1\n");
   
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = getelementptr i32, i32* %_"+array_name+", i32 %_"+(counter-1)+"\n");
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
   expr_reg_counter=counter;
   MyWriter.write("  br label %oob"+(oob+3)+"\n");

   MyWriter.write("oob"+(oob+2)+":\n\n");
   MyWriter.write("  call void @throw_oob()\n");
   MyWriter.write("  br label %oob"+(oob+3)+"\n");

   MyWriter.write("oob"+(oob+3)+":\n\n");



   return "lookup";
}

/**
* f0 -> PrimaryExpression()
* f1 -> "."
* f2 -> "length"
*/
@Override public String visit(ArrayLength n, Void argu) throws Exception {
   String _ret=null;
   n.f0.accept(this, argu);
   n.f1.accept(this, argu);
   n.f2.accept(this, argu);
   return _ret;
}

/**
* f0 -> PrimaryExpression()
* f1 -> "."
* f2 -> Identifier()
* f3 -> "("
* f4 -> ( ExpressionList() )?
* f5 -> ")"
*/
@Override public String visit(MessageSend n, Void argu) throws Exception {
   
   String s = n.f0.accept(this, argu);
   String c = null;
   int counter = method_reg_counter;
   int method_offset;
   
   String methodname = n.f2.accept(this, argu);


   //if s = this
   if(this_expression==1){
      MyWriter.write("  %_"+counter+" = bitcast i8* %this to i8***\n");
      //counter = ++method_reg_counter;
      //MyWriter.write("  %_"+counter+" = load i8**, i8***"+" %_"+(counter-1)+"\n");
      c = "%this";
      MessageSendItem=currclass;
   }

   //if s = item

   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      MyWriter.write("  %_"+counter+" = load i8*, i8** %"+s);
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+counter+" to i8***");
      c = "%s";
   }
   //if s = a register

   else if(is_reg==1 && expr_reg_counter!=-1){
      c = "%_"+expr_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+expr_reg_counter+" to i8***");
      String array[] = s.split("new");
      MessageSendItem=array[1];
   }
   System.out.println(MessageSendItem+methodname);
   String method_type=llvmType(st.ClassCollector.get(MessageSendItem).getMethodType(methodname));

   
   method_offset=vt.getMethodIndex(MessageSendItem, methodname);
   MyWriter.write("\n ; "+MessageSendItem+"."+methodname+" : "+method_offset+" \n");
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = load i8**, i8*** %_"+(counter-1)+"\n");
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = getelementptr i8*, i8** %_"+(counter-1)+", i32 "+method_offset+"\n");
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = load i8*, i8** %_"+(counter-1)+"\n");
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+method_type+" (i8*");
   int caller=counter;

   counter=++method_reg_counter;

   String params = st.getMethodParams(MessageSendItem, methodname).values().toString();   //get the parameters of the function of that type
   params = params.replace("[", "");
   params = params.replace("]", "");
   params = params.replace(" ", "");

   String[] args = params.split(",");
   for(String arg : args){
      if(!arg.equals("null")){
         MyWriter.write(", "+llvmType(arg));
      }
   }
   MyWriter.write(" )*\n");

   String input =  n.f4.present() ? n.f4.accept(this, null) : "null";  //hold arguments

   input = input.replace("[", "");
   input = input.replace("]", "");
   input = input.replace(" ", "");

   String[] inputs = input.split(",");
   if(array_helper==null){
      return null;
   }
   String[] input_types = array_helper.split(",");
   
   int inputs_size=inputs.length;
   for(int i=0; i<inputs_size; i++){
      if(inputs[i].equals("null")){
         continue;
      }
      else if(input_types[i].equals("reg")){
         counter=++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load "+llvmType(args[i])+" , "+llvmType(args[i])+"* %"+inputs[i]+"\n");
         inputs[i] = "%_"+counter;
      }
      else if(input_types[i].equals("reg_")){
         counter=++method_reg_counter;
         inputs[i] = "%_"+inputs[i];

      }
   }
   counter=++method_reg_counter;
   MyWriter.write("  %_"+counter+" = call "+method_type+" %_"+caller+"(i8* "+c+" ");

   for(int i=0; i<inputs_size; i++){
      if(!args[i].equals("null")){
         MyWriter.write(", "+llvmType(args[i])+" "+inputs[i]);
      }
   }

   MyWriter.write(")\n");
   expr_reg_counter=counter;

   return "messagesend";
}

/**
* f0 -> Expression()
* f1 -> ExpressionTail()
*/
@Override public String visit(ExpressionList n, Void argu) throws Exception {
   String s=  n.f0.accept(this, argu);

   if(s!=null){
      if(s.equals("int") && integer_expression!=-1){
         s= Integer.toString(integer_expression);
         array_helper="int";
      }
      else if(s.equals("boolean") && boolean_expression!=-1){
         if(boolean_expression==0){
            s="0";
            array_helper="boolean";

         }
         else{ 
            s="1";
            array_helper="boolean";

         }
      }
      else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
         array_helper="reg";
      }

      else if(is_reg==1 && expr_reg_counter!=-1){

         s=""+expr_reg_counter;
         array_helper="reg_";
      }
   }
   String List = s;
   List+= n.f1.accept(this, null);
   return List;
}

/**
* f0 -> ( ExpressionTerm() )*
*/
@Override public String visit(ExpressionTail n, Void argu) throws Exception {
   String s="";
   for( Node node: n.f0.nodes){
      String nd = node.accept(this, null);
      if(nd!=null){
         if(nd.equals("int") && integer_expression!=-1){
            s+= ","+integer_expression;
            array_helper+=",int";
         }
         else if (nd.equals("boolean") && integer_expression!=-1){
            if(boolean_expression==0){
               s+="0";
               array_helper+=",boolean";

            }
            else 
               s+="1";
               array_helper+=",boolean";
         }
         else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(nd) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(nd)){
            s+=","+node.accept(this, null);
            array_helper+=",reg";
         }
         else if(st.ClassCollector.get(currclass).getVars().containsKey(nd)){
            s+=","+node.accept(this, null);
            array_helper+=",reg";
         }

         else if(is_reg==1 && expr_reg_counter!=-1){
            s+=","+expr_reg_counter;
            array_helper+=",reg_";

         }
      }
   }
   return s;   
}

/**
* f0 -> ","
* f1 -> Expression()
*/
@Override public String visit(ExpressionTerm n, Void argu) throws Exception {
   String  s = n.f1.accept(this, null);
   return s;
}

/**
* f0 -> NotExpression()
*       | PrimaryExpression()
*/
@Override public String visit(Clause n, Void argu) throws Exception {
   return n.f0.accept(this, argu);
}

/**
* f0 -> IntegerLiteral()
*       | TrueLiteral()
*       | FalseLiteral()
*       | Identifier()
*       | ThisExpression()
*       | ArrayAllocationExpression()
*       | AllocationExpression()
*       | BracketExpression()
*/
@Override public String visit(PrimaryExpression n, Void argu) throws Exception {
   String s = n.f0.accept(this, argu);

   return s;
}

/**
* f0 -> <INTEGER_LITERAL>
*/
@Override public String visit(IntegerLiteral n, Void argu) throws Exception {
   integer_expression =  Integer.parseInt(n.f0.toString());
   return "int";
}

/**
* f0 -> "true"
*/
@Override public String visit(TrueLiteral n, Void argu) throws Exception {
   boolean_expression = 1;
   return "boolean";
}

/**
* f0 -> "false"
*/
@Override public String visit(FalseLiteral n, Void argu) throws Exception {
   boolean_expression = 0;
   return "boolean";
}

/**
* f0 -> <IDENTIFIER>
*/
@Override public String visit(Identifier n, Void argu) throws Exception {
   return n.f0.tokenImage;
}

/**
* f0 -> "this"
*/
@Override public String visit(ThisExpression n, Void argu) throws Exception {
   this_expression = 1;
   return currclass;
}

/**
* f0 -> BooleanArrayAllocationExpression()
*       | IntegerArrayAllocationExpression()
*/
@Override public String visit(ArrayAllocationExpression n, Void argu) throws Exception {
   String s = n.f0.accept(this, argu);
   is_reg=1;
   return s;

}

/**
* f0 -> "new"
* f1 -> "boolean"
* f2 -> "["
* f3 -> Expression()
* f4 -> "]"
*/
@Override public String visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
      
   
      //a lot of bugs

   String s = n.f3.accept(this, argu);
   int arr_alloc=-1;
   int array_size=-1;
   int counter=-1;
   int reg=-1;
   //if expr is an int

   if(s.equals("int")){
      if(integer_expression!=-1){
         MyWriter.write("  store i32 "+integer_expression+" , i32* %_"+method_reg_counter+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         counter=++method_reg_counter;
         MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
         array_size=counter;
         MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
         counter = ++method_reg_counter;
         counter = ++method_reg_counter;

         arr_alloc=method_reg_counter;

      }
   }

   //if exp is id in method args or in method vars

   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      array_size=method_reg_counter;
      MyWriter.write("  %_"+method_reg_counter+" = load i32, i32* %"+s+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;
      
      arr_alloc=method_reg_counter;

   }

   //if exp is in class variables

   else if (st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      
      MyWriter.write("\n  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32*, i32** %_"+(counter-1)+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

      arr_alloc=method_reg_counter;
   }
   
   else if(is_reg==1 && expr_reg_counter!=-1){
      counter=++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32*, i32** %_"+(counter-1)+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

      arr_alloc=method_reg_counter;
   }

   counter = ++method_reg_counter;

   MyWriter.write("arr_alloc"+(arr_alloc-1)+":\n");
   MyWriter.write("  call void @throw_oob()\n");
   MyWriter.write("  br label %arr_alloc"+(arr_alloc)+"\n");

   MyWriter.write("arr_alloc"+arr_alloc+":\n");
   MyWriter.write("  %_"+counter+" = add i32 %_"+array_size+", 1\n");
   counter = ++method_reg_counter;

   MyWriter.write("  %_"+counter+" = call i8* @calloc(i32 1, i32 %_"+(counter-1)+")\n");
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to i8*\n");
   MyWriter.write("  store i32 %_"+array_size+", i8* %_"+counter+"\n\n");
   
   
   expr_reg_counter=counter; //here is the register to be returned
   
   is_reg=1;
   return "boolean[]";
}

/**
* f0 -> "new"
* f1 -> "int"
* f2 -> "["
* f3 -> Expression()
* f4 -> "]"
*/
@Override public String visit(IntegerArrayAllocationExpression n, Void argu) throws Exception {
   
   int arr_alloc=-1;
   int array_size=-1;
   int counter=-1;
   is_reg=0;
   String s = n.f3.accept(this, argu);

   //if expr is an int

   if(s.equals("int")){
      if(integer_expression!=-1){
         MyWriter.write("  store i32 "+integer_expression+" , i32* %_"+method_reg_counter+"\n");
         counter = ++method_reg_counter;
         MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
         MyWriter.write("  %_"+(counter+1) +" = icmp slt i32 %_"+counter+", 0\n");
         array_size=counter;
         counter = ++method_reg_counter;
         MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
         counter = ++method_reg_counter;
         counter = ++method_reg_counter;

         arr_alloc=method_reg_counter;

      }
   }

   //if exp is id in method args or in method vars

   else if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      array_size=method_reg_counter;
      MyWriter.write("  %_"+method_reg_counter+" = load i32, i32* %"+s+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      arr_alloc=++method_reg_counter;

   }


   //if exp is id in class variables
   else if (st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;

      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      
      MyWriter.write("\n  %_"+method_reg_counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
      array_size=counter;
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;

      arr_alloc=method_reg_counter;
   }

   else if(is_reg==1 && expr_reg_counter!=-1){
      counter=++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i32, i32* %_"+(counter-1)+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter +" = icmp slt i32 %_"+(counter-1)+", 0\n");
      MyWriter.write("  br i1 %_"+counter+", label %arr_alloc"+ (counter+1)+", label %arr_alloc"+(counter+2)+"\n\n");
      counter = ++method_reg_counter;
      counter = ++method_reg_counter;
      arr_alloc=method_reg_counter;
   }

   counter = ++method_reg_counter;

   MyWriter.write("arr_alloc"+(arr_alloc-1)+":\n");
   MyWriter.write("  call void @throw_oob()\n");
   MyWriter.write("  br label %arr_alloc"+(arr_alloc)+"\n");

   MyWriter.write("arr_alloc"+arr_alloc+":\n");
   MyWriter.write("  %_"+counter+" = add i32 %_"+array_size+", 1\n");
   counter = ++method_reg_counter;

   MyWriter.write("  %_"+counter+" = call i8* @calloc(i32 4, i32 %_"+(counter-1)+")\n");
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to i32*\n");
   MyWriter.write("  store i32 %_"+array_size+", i32* %_"+counter+"\n\n");

   expr_reg_counter=counter; //here is the register to be returned
   is_reg=1;

   ++method_reg_counter;
   return "int[]";
}

/**
* f0 -> "new"
* f1 -> Identifier()
* f2 -> "("
* f3 -> ")"
*/
@Override public String visit(AllocationExpression n, Void argu) throws Exception {
   
   String s = n.f1.accept(this, argu);
   int offset = st.getLastVarOffset(s);
   int lastvarsize =  st.getLastVarSize(s);
   int counter = method_reg_counter; 
   MessageSendItem=s;   //save for message send
   expr_reg_counter=counter; //here is the register to be returned

   MyWriter.write("  %_"+counter+" = call i8* @calloc(i32 1, i32 "+(offset+8+lastvarsize)+(")\n"));
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to i8***\n");
   counter = ++method_reg_counter;
   MyWriter.write("  %_"+counter+" = getelementptr [ "+vt.getVtsize(s)+" x i8*], [ "+vt.getVtsize(s)+" x i8*]* @."+s+"_vtable, i32 0, i32 0 \n");
   
   MyWriter.write("  store i8** %_"+counter+", i8*** %_"+(counter-1)+" \n");
   ++method_reg_counter;
   is_reg=1;

   return "new"+s;

}

/**
* f0 -> "!"
* f1 -> Clause()
*/
@Override public String visit(NotExpression n, Void argu) throws Exception {

   String s = n.f1.accept(this, argu);
   int counter=method_reg_counter;

   if(s==null){
      return null;
   }

   if(st.ClassCollector.get(currclass).getMethodParams(currmethod).containsKey(s) || st.ClassCollector.get(currclass).getMethodVars(currmethod).containsKey(s)){
      MyWriter.write("  %_"+counter+" = load i1, i1* %"+s+"\n");
      MyWriter.write("  %_"+(counter+1)+" = xor i1 1, %_"+counter+"\n");
      counter=++method_reg_counter;
      expr_reg_counter=counter;
   } 
   
   else if(st.ClassCollector.get(currclass).getVars().containsKey(s)){
      int offset = st.getVarOffsets(currclass).get(s) + 8;
      String llvm_type = llvmType(st.searchType(currclass, currmethod, s));
      MyWriter.write("  %_"+counter+" = getelementptr i8, i8* %this, i32 "+ offset+ "\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = bitcast i8* %_"+(counter-1)+" to "+llvm_type+"*\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = load i1, i1* %_"+(counter-1)+"\n");
      counter = ++method_reg_counter;
      MyWriter.write("  %_"+counter+" = xor i1 1, %_"+(counter-1)+"\n");
      expr_reg_counter=counter;

   }
   else if(expr_reg_counter!=-1){
      MyWriter.write("  %_"+counter+" = xor i1 1, %_"+(expr_reg_counter)+"\n");
      expr_reg_counter=counter;
   }
   is_reg=1;

   ++method_reg_counter;
   return "not";
}

/**
* f0 -> "("
* f1 -> Expression()
* f2 -> ")"
*/
@Override public String visit(BracketExpression n, Void argu) throws Exception {
   String s = n.f1.accept(this, argu);
   return s;
   }
}



