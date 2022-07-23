
//PANAGIOTIS KONTOEIDIS
//1115201900266
import syntaxtree.*;
import visitor.*;

import java.lang.reflect.Method;
import java.util.*;

public class MyVisitor2  extends GJDepthFirst<String, Void>{
    SymbolTable st;
    int method_helper = 0 ;
    String currmethod;   //variable to hold current method
    String currclass;      //variable to hold current class;
    public MyVisitor2(MyVisitor myVisitor){
        st = myVisitor.st;
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
        n.f0.accept(this, argu);
        String s = n.f1.accept(this, argu);
        currclass =s;
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        currmethod = "main";
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);

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
        n.f0.accept(this, argu);
        String s = n.f1.accept(this, argu);
        currclass = s;

        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        method_helper = 0 ;
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
        String s = n.f1.accept(this, argu);
        currclass = s;
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        method_helper = 0;
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

        if(!vartype.equals("int") && !vartype.equals("boolean")&& !vartype.equals("boolean[]")&&!vartype.equals("int[]")){
            if(!st.isDefinedClass(vartype)){  
                throw new Error("Variable "+varname+" has illegal type"+vartype);
            }
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
      n.f0.accept(this, argu);
      String type = n.f1.accept(this, argu);

      currmethod = n.f2.accept(this, argu);
      
      n.f3.accept(this, argu);
      String argumentList = n.f4.present() ? n.f4.accept(this, null) : "null";
        
      if(argumentList!="null"){
         String[] temp = argumentList.split(",");
        
         for(String arg : temp){
               String params[] = arg.split("=");
               String partype = params[0].trim();
               String paramname = params[1].trim();

               if(!partype.equals("int") && !partype.equals("boolean") && !partype.equals("boolean[]") && !partype.equals("int[]")){
                  if(!st.isDefinedClass(partype)){
                     throw new Error("Parameter "+paramname+" has illegal type "+partype);
                  }
               }
            }
        }
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        String s = n.f10.accept(this, argu);
        String returntype = st.searchType(currclass, currmethod, s);


        if(returntype.equals("null")){   //check return type
           throw new Error("Return Type in method "+currmethod+" is not valid");
        }
        if(!returntype.equals(type)){//check return type
           throw new Error("Return type does not match declared method type in method "+currmethod);
        }
        
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);

        if(!st.getClassParent(currclass).equals("null")){
            String parent = st.getClassParent(currclass);

            if(st.getMethodnames(parent).contains(currmethod)){
               
               String argscurr = st.getMethodParams(currclass, currmethod).values().toString();
               argscurr = argscurr.replace("[", "");
               argscurr = argscurr.replace("]", "");

               String argsparent = st.getMethodParams(parent, currmethod).values().toString();
               argsparent = argsparent.replace("[", "");
               argsparent = argsparent.replace("]", "");

                if(!argscurr.equals(argsparent)){    //look for inheritance
                  throw new Error("methods are not inherently polymorphic because arguments do not match");
                }                               //look for inheritance
                if(!st.getMethodType(currclass, currmethod).equals(st.getMethodType(parent, currmethod))){
                     throw new Error("methods are not inherently polymorphic because types do not match");
                }
            }             
         }
         method_helper = 1;



        return null;
     }
  
     /*
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
  
     @Override
     public String visit(BooleanArrayType n, Void argu) {
         return "boolean[]";
         }
 
     @Override
     public String visit(IntegerArrayType n, Void argu) {
         return "int[]";
     }
 
     @Override public String visit(BooleanType n, Void argu) {
         return "boolean";
     }
 
     @Override public String visit(IntegerType n, Void argu) {
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
        return n.f0.accept(this, argu);
     }
  
     /**
      * f0 -> "{"
      * f1 -> ( Statement() )*
      * f2 -> "}"
      */
     @Override public String visit(Block n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String s = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return s;
     }
  
     /**
      * f0 -> Identifier()
      * f1 -> "="
      * f2 -> Expression()
      * f3 -> ";"
      */
     @Override public String visit(AssignmentStatement n, Void argu) throws Exception {
        String s1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String s2 = n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        String type1 = st.searchType(currclass, currmethod, s1);

        String type2 = st.searchType(currclass, currmethod, s2);

        if(type1.equals("null")){
           throw new Error("variable "+ s1+" not found");
        }
        if(type2.equals("null")){
           throw new Error("variable "+ s2+" not found");
        }


      if(!type1.equals(type2)){
         if(st.isDefinedClass(type1) && st.isDefinedClass(type2)){
            if(st.getClassParent(type2).equals(type1)){
               return null;
            }
            else
               throw new Error("variable "+s1+ " assignment is not valid");

         }
         else
            throw new Error("variable "+s1+ " assignment is not valid");
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


      String s = n.f0.accept(this, argu);
      String type = st.searchType(currclass, currmethod, s);

      if(type.equals("null")){
         throw new Error("variable "+ s+" not found");
      }
      if(!type.equals("boolean[]")&&!(type.equals("int[]"))){
         throw new Error("variable "+s+ " in assignment expression is not an array");
      }

      String s1 = n.f2.accept(this, argu);
      String type1 = st.searchType(currclass, currmethod, s1);

      if(type1.equals("null")){
         throw new Error("variable "+ s1+" not found");
      }
      if(!type1.equals("int")){
         throw new Error("variable "+s1+ " is not type int");
      }
      String s2 = n.f5.accept(this, argu);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type2.equals("null")){
         throw new Error("variable "+ s2+" not found");
      }
      if(type.equals("int[]")){   
         if(!type2.equals("int")){
            throw new Error("variable "+s2+ " is not type int");
         }
      }
      if(type.equals("boolean[]")){   
         if(!type2.equals("boolean")){
            throw new Error("variable "+s2+ " is not type boolean");
         }
      }
        
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


        String s = n.f2.accept(this, argu);

        String type1 = st.searchType(currclass, currmethod, s);

        if(type1.equals("null")){
           throw new Error("variable "+ s+" not found");
        }
        if(!type1.equals("boolean")){
           throw new Error("variable "+s+ " in Add expression is not type boolean");
        }

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
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

      String s1 = n.f2.accept(this, argu);

      String type1 = st.searchType(currclass, currmethod, s1);

      if(type1.equals("null")){
         throw new Error("variable "+ s1+" not found");
      }
      if(!type1.equals("boolean")){
         throw new Error("variable "+s1+ " in Add expression is not type boolean");
      }
      
      n.f4.accept(this, argu);

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
        String _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String s = n.f2.accept(this, argu);
        System.out.println(currclass+currmethod+s);
        String type = st.searchType(currclass, currmethod, s);

         if(type.equals("null")){
            throw new Error("variable "+ s+" not found");
         }
         if(!type.equals("int")){
            throw new Error("variable "+s+ " can not be printed");
         }

        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return _ret;
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
        return n.f0.accept(this, argu);
     }
  
     /**
      * f0 -> Clause()
      * f1 -> "&&"
      * f2 -> Clause()
      */
     @Override public String visit(AndExpression n, Void argu) throws Exception {
      String s1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String s2 = n.f2.accept(this, argu);
      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == null){
         throw new Error("variable"+ s1+" not found");
      }
      if(type2 == null){
         throw new Error("variable"+ s2+" not found");
      }
      if(!type1.equals("boolean")){
         throw new Error("variable "+s1+ " in Add expression is not type boolean");
      }
      if(!type2.equals("boolean")){
         throw new Error("variable "+s2+ " in Add expression is not type boolean");
         }
      return "boolean";
      }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "<"
      * f2 -> PrimaryExpression()
      */
     @Override public String visit(CompareExpression n, Void argu) throws Exception {
      
      String s1 = n.f0.accept(this, argu);
      String s2 = n.f2.accept(this, argu);
      
      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == null){
         throw new Error("variable "+s1+" not found");
      }
      if(type2==null){
         throw new Error("variable "+ s2 +" not found");
      }
      if(!type1.equals("int")){
         throw new Error("variable "+s1+ " in Add expression is not type boolean");
      }
      if(!type2.equals("int")){
         throw new Error("variable "+s2+ " in Add expression is not type boolean");
         }
      return "boolean";
      }


     /**
      * f0 -> PrimaryExpression()
      * f1 -> "+"
      * f2 -> PrimaryExpression()
      */
     @Override public String visit(PlusExpression n, Void argu) throws Exception {
      String s1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String s2 = n.f2.accept(this, argu);

      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == "null"){
         throw new Error("variable "+s1+" not found");
      }
      if(type2=="null"){
         throw new Error("variable "+ s2 +" not found");
      }
      if(!type1.equals("int")){
         throw new Error("variable "+s1+ " in Plus expression is not type boolean");
      }
      if(!type2.equals("int")){
         throw new Error("variable "+s2+ " in Plus expression is not type boolean");
         }
      return "int";
      }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "-"
      * f2 -> PrimaryExpression()
      */
     @Override public String visit(MinusExpression n, Void argu) throws Exception {
      String s1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String s2 = n.f2.accept(this, argu);

      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == "null"){
         throw new Error("variable "+ s1+" not found");
      }
      if(type2 == "null"){
         throw new Error("variable "+ s2+" not found");
      }
      if(!type1.equals("int")){
         throw new Error("variable "+s1+ " in Minus expression is not type boolean");
      }
      if(!type2.equals("int")){
         throw new Error("variable "+s2+ " in Minus expression is not type boolean");
         }
      return "int";
      }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "*"
      * f2 -> PrimaryExpression()
      */
     @Override public String visit(TimesExpression n, Void argu) throws Exception {
      String s1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String s2 = n.f2.accept(this, argu);
      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == "null"){
         throw new Error("variable "+ s1+" not found");
      }
      if(type2 == "null"){
         throw new Error("variable "+ s2+" not found");
      }
      if(!type1.equals("int")){
         throw new Error("variable "+s1+ " in Times expression is not type boolean");
      }
      if(!type2.equals("int")){
         throw new Error("variable "+s2+ " in Times expression is not type boolean");
         }
      return "int";
      }
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "["
      * f2 -> PrimaryExpression()
      * f3 -> "]"
      */
     @Override public String visit(ArrayLookup n, Void argu) throws Exception {
      String s1 = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String s2 = n.f2.accept(this, argu);

      String type1 = st.searchType(currclass, currmethod, s1);
      String type2 = st.searchType(currclass, currmethod, s2);
      if(type1 == "null"){
         throw new Error("variable "+ s1+" not found");
      }
      if(type2 == "null"){
         throw new Error("variable "+ s2+" not found");
      }
      if(!type1.equals("int[]")&&!type1.equals("boolean[]")){
         throw new Error("variable "+s1+ " in Lookup expression is not type boolean");
      }
      if(!type2.equals("int")){
         throw new Error("variable "+s2+ " in Lookup expression is not type int");
         }

      if(type1.equals("int[]"))
         return "int";

      if(type1.equals("boolean[]"))
         return "boolean";
      return null;
      }
  
  
     /**
      * f0 -> PrimaryExpression()
      * f1 -> "."
      * f2 -> "length"
      */
     @Override public String visit(ArrayLength n, Void argu) throws Exception {
         int a=0;  
         String s = n.f0.accept(this, argu);

         String type1 = st.searchType(currclass, currmethod, s);
         if(type1 == "null"){
            throw new Error("variable "+s+" not found");
         }
         if(!type1.equals("int[]") && !type1.equals("boolean[]")){
            throw new Error("variable "+s+ " in Length expression is not an array");
         }

         return "int";
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
         String _ret=null;

         String s = n.f0.accept(this, argu);    //hold primary exp
        
         String s1 = n.f2.accept(this, argu);   //hold id 

         String exprList = n.f4.present() ? n.f4.accept(this, null) : "null";  //hold arguments

         String temp = st.searchType(currclass, currmethod, s);  //search type of primary exp

         String exp[] = exprList.split(",");


         if(!temp.equals("null")){  //if primary exp returns a type

            if(st.getMethodnames(temp).contains(s1)){   
               String args = st.getMethodParams(temp, s1).values().toString();   //get the parameters of the function of that type
               args = args.replace("[", "");
               args = args.replace("]", "");

               String[] arg = args.split(",");

               if(exp.length != arg.length){   
                  throw new Error("Wrong number of arguments");
               }

               for(int i=0;i<exp.length;i++){
                  if(!exp[i].equals(arg[i])){
                     if(st.MessageSendParentMethods(exp[i], arg[i]).equals("null")){
                        throw new Error("Arguments for method: "+s1+", do not match");
                     }
                     else  
                        System.out.println();
                  }
               }
               return st.getMethodType(temp, s1);               
            }
            if(!st.MessageSendParent(temp,s1).equals("null")){  //if primary func do not contain func s1
                return st.MessageSendParent(temp,s1);
            }

            else{ 
               throw new Error("Method "+s+" is undefined for class "+temp);
            }
         }
         return _ret;
      }
  
     /**
      * f0 -> Expression()
      * f1 -> ExpressionTail()
      */
     @Override public String visit(ExpressionList n, Void argu) throws Exception {
        String s=  n.f0.accept(this, argu);
        String List = st.searchType(currclass, currmethod, s);
        
        if (n.f1 != null) {
         List += n.f1.accept(this, null);
         }        
         return List;
     }
  
     /**
      * f0 -> ( ExpressionTerm() )*
      */
     @Override public String visit(ExpressionTail n, Void argu) throws Exception {
      String s = "";
      for (Node node: n.f0.nodes){
         s += ", "+node.accept(this, null);
      }  
      return s;
     }
  
     /**
      * f0 -> ","
      * f1 -> Expression()
      */
     @Override public String visit(ExpressionTerm n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String s = n.f1.accept(this, argu);
        String type = st.searchType(currclass, currmethod, s);
        return type;
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
        return n.f0.accept(this, argu);
     }
  
     /**
      * f0 -> <INTEGER_LITERAL>
      */
     @Override public String visit(IntegerLiteral n, Void argu) throws Exception {
        return "int";
     }
  
     /**
      * f0 -> "true"
      */
     @Override public String visit(TrueLiteral n, Void argu) throws Exception {
        return "boolean";
     }
  
     /**
      * f0 -> "false"
      */
     @Override public String visit(FalseLiteral n, Void argu) throws Exception {
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
        return currclass;
     }
  
     /**
      * f0 -> BooleanArrayAllocationExpression()
      *       | IntegerArrayAllocationExpression()
      */
     @Override public String visit(ArrayAllocationExpression n, Void argu) throws Exception {
         return n.f0.accept(this, argu);
     }
  
     /**
      * f0 -> "new"
      * f1 -> "boolean"
      * f2 -> "["
      * f3 -> Expression()
      * f4 -> "]"
      */
     @Override public String visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        String s = n.f3.accept(this, argu);

        String type1 = st.searchType(currclass, currmethod, s);

        if(type1.equals("null") ){
           throw new Error("variable "+s+" not found");
        }
        if(!type1.equals("int")){
           throw new Error("variable "+s+ " in Allocation expression is not type boolean");
        }

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
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        String s = n.f3.accept(this, argu);

        String type1 = st.searchType(currclass, currmethod, s);

        if(type1.equals("null") ){
           throw new Error("variable "+s+" not found");
        }
        if(!type1.equals("int")){
           throw new Error("variable "+s+ " in Allocation expression is not type int");
        }
        return "int[]";
        }
    

  
     /**
      * f0 -> "new"
      * f1 -> Identifier()
      * f2 -> "("
      * f3 -> ")"
      */
     @Override public String visit(AllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String s = n.f1.accept(this, argu);
        if(!st.isDefinedClass(s))
            throw new Error("Can not Allocate "+s +" Object ");
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return s;  //return type of the Identifier
     }
  
     /**
      * f0 -> "!"
      * f1 -> Clause()
       */

      //this expression must return booleans 
     @Override public String visit(NotExpression n, Void argu) throws Exception {
        String s = n.f1.accept(this, argu);
        String type1 = st.searchType(currclass, currmethod, s);
        
        if(type1.equals("null")){
           throw new Error("variable "+s+" not found");
        }
        if(!type1.equals("boolean")){
           throw new Error("variable "+s+ " in Not expression is not type boolean");
        }
        return "boolean";
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
  

