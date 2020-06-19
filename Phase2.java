package com.company;
import java.io.*;
import java.util.HashMap;

public class Phase2 {
    HashMap<String, String> mapOpcodeToObjectcode = new HashMap<>();
    HashMap<String, String> mapRegisterWithItsNumber = new HashMap<>();
    BufferedWriter writer = new BufferedWriter(new FileWriter("object file.txt"));

    //String[][] phaseOneOutput = new String[90][6];
    String number = "", address = "", label =  "",  opcode = "", operand = "";
    int index = 0,zFlag = 0, r1, r2;
    String DecimalToHExa = "";
    String fisrtaddress="";
    String objCode="";
    String prevobjCode="";
    String textaddress="";
    String textstring="";
    String basereg="1500";
    String literalstring="";


    Logic logic;

    public Phase2(Logic log) throws IOException {
        logic = log;
    }


    private void generatingOpcode() {
        mapOpcodeToObjectcode.put("ADD", "18");
        mapOpcodeToObjectcode.put("ADDR", "90");
        mapOpcodeToObjectcode.put("SUB", "1C");
        mapOpcodeToObjectcode.put("SUBR", "94");
        mapOpcodeToObjectcode.put("COMP", "28");
        mapOpcodeToObjectcode.put("COMPR", "A0");
        mapOpcodeToObjectcode.put("J", "3C");
        mapOpcodeToObjectcode.put("JEQ", "30");
        mapOpcodeToObjectcode.put("JGT", "34");
        mapOpcodeToObjectcode.put("JLT", "38");
        mapOpcodeToObjectcode.put("LDA", "00");
        mapOpcodeToObjectcode.put("LDL", "08");
        mapOpcodeToObjectcode.put("LDS", "6C");
        mapOpcodeToObjectcode.put("LDT", "74");
        mapOpcodeToObjectcode.put("LDX", "04");
        mapOpcodeToObjectcode.put("LDB", "68");
        mapOpcodeToObjectcode.put("TIX", "2C");
        mapOpcodeToObjectcode.put("TIXR", "8B");
        mapOpcodeToObjectcode.put("RMO", "AC");

        mapRegisterWithItsNumber.put("A", "0");
        mapRegisterWithItsNumber.put("X", "1");
        mapRegisterWithItsNumber.put("L", "2");
        mapRegisterWithItsNumber.put("B", "3");
        mapRegisterWithItsNumber.put("S", "4");
        mapRegisterWithItsNumber.put("T", "5");
        mapRegisterWithItsNumber.put("F", "6");
        mapRegisterWithItsNumber.put("PC", "8");
        mapRegisterWithItsNumber.put("SW", "9");
    }

    protected void readcopyfile() throws FileNotFoundException, IOException {
        generatingOpcode();
        File file = new File("output.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String readWord;


        int st = 0;
        while ((readWord = br.readLine()) != null) {
            int w=0;
            if(st == 0)
            {
                st++;
                continue;
            }
            if(st == 1){
                st++;
                continue;
            }
            int enter=0;

            if(readWord.contains("*")){
                //textstring=textstring.substring(0,textstring.length()-prevobjCode.length());
           opcode="";
            }
            else if (opcode!=""){
                w=1;
                GenerateObjCode();
                if(opcode.toUpperCase().equals("RESW")||opcode.toUpperCase().equals("RESB")){

                    writenewTextObjectFile();

                }else
                 writeTextObjectFile();

            }

            String s = readWord.substring(0,18).trim();
            if(s.equals(""))
                continue;
            if(Character.isDigit(s.charAt(0))){
                number = s;

                address = readWord.substring(18,35).trim();
                label = readWord.substring(35,50).trim();
                opcode = readWord.substring(50,66).trim();
                operand = readWord.substring(66,92).trim();

/*

                if(opcode.toUpperCase().equals("END"))
                    break;
*/

                //evaluateOperand();
                if (opcode.toUpperCase().equals("START")){
                    fisrtaddress=address;

                    textaddress=fisrtaddress;
                    writeHeaderObjectFile();
                   // writenewTextObjectFile();
                }

                //GenerateObjCode();




            }
        }
        writenewTextObjectFile();
        writeEndObjectFile();
        writer.close();
    }

    public void GenerateObjCode() throws IOException {

        objCode = "";

        int formatnum = 0;
        //literals
        if (operand.charAt(0) == '=') {
            if (operand.charAt(1) == 'X') {
                objCode = operand.substring(3, operand.length() - 1);

            } else if (operand.charAt(1) == 'C') {
                //ascii
                operand = operand.substring(3, operand.length() - 1);
                for (int i = 0; i < operand.length(); i++) {
                    objCode = objCode + (int) (operand.charAt(i));
                }

            } else {
                //w
                objCode = logic.DecimalToHexa(Integer.parseInt(operand.substring(3, operand.length() - 1)));
            }
            literalstring = literalstring + objCode;
            objCode = "--";
        }
/////////////////


        else if (logic.mapDirective.containsKey(opcode.toUpperCase()) || opcode.toUpperCase().equals("START") || opcode.toUpperCase().equals("END")) {

            if (opcode.toUpperCase().equals("WORD") || opcode.toUpperCase().equals("BYTE")) {


                if (opcode.toUpperCase().equals("WORD")) {
                    System.out.println("WORD:");

                    objCode = logic.DecimalToHexa(Integer.parseInt(operand));

                } else if (opcode.toUpperCase().equals("BYTE")) {
                    System.out.println("BYTE:");
                    if (operand.charAt(0) == 'c') {
                        //ascii
                        operand = operand.substring(2, operand.length() - 1);


                        for (int i = 0; i < operand.length(); i++) {
                            objCode = objCode + (int) (operand.charAt(i));

                        }
                        objCode = logic.DecimalToHexa(Integer.parseInt(objCode));
                    } else if (operand.charAt(0) == 'X') {
                        objCode = operand.substring(2, operand.length() - 1);

                    }

                }
                if (objCode.length() != 6) {
                    if (objCode.length() > 6) objCode = objCode.substring(0, 6);
                    else {
                        if (objCode.length() == 5) objCode = "0" + objCode;
                        else if (objCode.length() == 4) objCode = "00" + objCode;
                        else if (objCode.length() == 3) objCode = "000" + objCode;
                        else if (objCode.length() == 2) objCode = "0000" + objCode;
                        else if (objCode.length() == 1) objCode = "00000" + objCode;
                    }
                }

            } else
                objCode = "--";

        }


        //Format 2

        else if (opcode.toUpperCase().equals("RMO") || opcode.toUpperCase().equals("TIXR") || opcode.toUpperCase().equals("SUBR") || opcode.toUpperCase().equals("COMPR") || opcode.toUpperCase().equals("ADDR")) {
            System.out.println("Format 2: ");
            System.out.println(opcode + " " + operand);

            if (opcode.toUpperCase().equals("ADDR") || opcode.toUpperCase().equals("SUBR") || opcode.toUpperCase().equals("RMO") || opcode.toUpperCase().equals("COMPR")) {
                objCode = mapOpcodeToObjectcode.get(opcode) + mapRegisterWithItsNumber.get(operand.charAt(0) + "") + mapRegisterWithItsNumber.get(operand.charAt(2) + "");

            }
            if (opcode.toUpperCase().equals("TIXR")) {
                objCode = mapOpcodeToObjectcode.get(opcode) + mapRegisterWithItsNumber.get(operand.charAt(0) + "") + 0;

            }

        }
        //Format 4
        else if (opcode.charAt(0) == '+') {
            formatnum = 4;
            System.out.println("Format 4: ");
            System.out.println(opcode + " " + operand);
            //operation code
            String n = "0", i = "0", x = "0";
            String b = "0", p = "0", e = "1";  //const.
            String address;


            if (operand.contains("+") || operand.contains("-") || operand.contains("/") || operand.contains("*")) {
                i = "1";
                n = "1";
                String finaladdress = "";
                if (operand.contains("+")) {
                    String op[] = operand.split("\\+");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) + logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("-")) {
                    String op[] = operand.split("\\-");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) - logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("/")) {
                    String op[] = operand.split("\\/");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) / logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("*")) {
                    String op[] = operand.split("\\*");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) * logic.HexToDEcimalConvert(op2address));

                }

                address = finaladdress;

            } else if (operand.charAt(0) == '#') {
                i = "1";
                address = operand.substring(1);
            } else if (operand.charAt(0) == '@') {
                n = "1";
                address = logic.mapSymbolTable.get(operand.substring(1));
            } else {
                //direct
                n = "1";
                i = "1";
                address = logic.mapSymbolTable.get(operand);
                //check if indexed
                if (operand.length() > 3 && operand.charAt(2) == 'X') {
                    x = "1";
                    address = logic.mapSymbolTable.get(operand.substring(0, operand.length() - 2));
                }
            }

            //objCode= mapOpcodeToObjectcode.get(opcode.substring(1).toUpperCase())+"    "+n+i+x+b+p+e+"    "+address;
           // objCode = logic.DecimalToHexa(logic.HexToDEcimalConvert(mapOpcodeToObjectcode.get(opcode.substring(1).toUpperCase())) + logic.HexToDEcimalConvert(n) + logic.HexToDEcimalConvert(i));
            //objCode=objCode+logic.DecimalToHexa(logic.HexToDEcimalConvert(x)+logic.HexToDEcimalConvert(b)+logic.HexToDEcimalConvert(p)+logic.HexToDEcimalConvert(e));
            //String objCode2 = x + b + p + e;
            //int decimal = Integer.parseInt(objCode2, 2);
           //// objCode2 = logic.DecimalToHexa(decimal);
           // objCode = objCode + objCode2 + address;


while (address.length()!=5){
address="0"+address;
}


            //objCode=mapOpcodeToObjectcode.get(opcode.substring(1).toUpperCase())+" "+n+i+x+b+p+e+" "+disp;
            String objCode1=logic.DecimalToHexa(logic.HexToDEcimalConvert(mapOpcodeToObjectcode.get(opcode.substring(1).toUpperCase())));
            String  objCode2=n+i;
            int decimal=Integer.parseInt(objCode2,2);
            objCode1=logic.DecimalToHexa(logic.HexToDEcimalConvert(objCode1)+decimal);
            //String objCode2=logic.DecimalToHexa(logic.HexToDEcimalConvert(x)+logic.HexToDEcimalConvert(b)+logic.HexToDEcimalConvert(p)+logic.HexToDEcimalConvert(e));
            objCode2=x+b+p+e;
            decimal=Integer.parseInt(objCode2,2);
            objCode2=logic.DecimalToHexa(decimal);
            if(objCode2=="")objCode2="0";
            objCode=objCode1+objCode2+address;



        }
        //Format 3
        else {

            formatnum = 3;
            System.out.println("FOrmat 3: ");
            System.out.println(opcode + " " + operand);
            String n = "0", i = "0", x = "0", b = "0", p = "0";
            String e = "0";  //const.
            String disp = "";

            if (operand.contains("+") || operand.contains("-") || operand.contains("/") || operand.contains("*")) {
                i = "1";
                b = "0";
                p = "1";
                x = "0";
                n = "1";
                String finaladdress = "";
                if (operand.contains("+")) {
                    String op[] = operand.split("\\+");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) + logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("-")) {
                    String op[] = operand.split("\\-");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) - logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("/")) {
                    String op[] = operand.split("\\/");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) / logic.HexToDEcimalConvert(op2address));

                } else if (operand.contains("*")) {
                    String op[] = operand.split("\\*");
                    String op1 = op[0];
                    String op1address = "";
                    String op2 = op[1];
                    String op2address = "";

                    if (!isNumeric(op1)) {
                        op1address = logic.mapSymbolTable.get(op1);
                    } else {
                        op1address = op1;
                    }
                    if (!isNumeric(op2)) {
                        op2address = logic.mapSymbolTable.get(op2);
                    } else {
                        op2address = op2;

                    }
                    finaladdress = logic.DecimalToHexa(logic.HexToDEcimalConvert(op1address) * logic.HexToDEcimalConvert(op2address));

                }

                int pc = logic.HexToDEcimalConvert(address) + 3;
                int ta = logic.HexToDEcimalConvert(finaladdress);
                disp = logic.DecimalToHexa(Math.abs(ta - pc));

            } else if (operand.charAt(0) == '#') {
                System.out.println("hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

                i = "1";
                b = "0";
                p = "0";
                x = "0";
                n = "0";
                disp = operand.substring(1);
                System.out.println(disp);
            }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            else if (operand.charAt(0) == '@') {
                i = "0";
                b = "0";
                p = "0";
                x = "0";
                n = "1";
                disp = operand.substring(1);
            }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            else if (operand.length() > 3 && operand.charAt(2) == 'X') {

                if (!isNumeric(operand.substring(0, operand.length() - 2))) {
                    i = "1";
                    b = "0";
                    p = "1";
                    x = "1";
                    n = "1";
                    int pc = logic.HexToDEcimalConvert(address) + 3;
                    int ta = logic.HexToDEcimalConvert(logic.mapSymbolTable.get(operand.substring(0, operand.length() - 2)));
                    disp = logic.DecimalToHexa(ta - pc);

                } else {
                    i = "1";
                    b = "0";
                    p = "0";
                    x = "1";
                    n = "1";
                    disp = operand.substring(0, operand.length() - 2);
                }

            }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //dirct but nmumbers
            else if (isNumeric(opcode)) {
                i = "1";
                b = "0";
                p = "1";
                x = "0";
                n = "1";
                int pc = logic.HexToDEcimalConvert(address) + 3;

                int ta = logic.HexToDEcimalConvert(operand);

                disp = logic.DecimalToHexa(Math.abs(ta - pc));
            }

////////// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //direct
            else {

                i = "1";
                b = "0";
                p = "1";
                x = "0";
                n = "1";
                int pc = logic.HexToDEcimalConvert(address) + 3;

                int ta = logic.HexToDEcimalConvert(logic.mapSymbolTable.get(operand));

                disp = logic.DecimalToHexa(Math.abs(ta - pc));
                System.out.println(opcode+"--------------");





            }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////end

            if(disp.length()!=3){
                if(disp.length()>3){
                   disp=disp.substring(disp.length()-3);
                }
                else {
                    int numofz=Math.abs(3-disp.length());
                    if(numofz==1){
                        disp="0"+disp;
                    }
                    else if(numofz==2){
                        disp="00"+disp;
                    }

                }
            }
            if(disp.equals("")) {
            disp="0000";
            }


//////////////////////////////////BASE
            if (logic.HexToDEcimalConvert(disp) > 2047) {
                p = "0";
                b = "1";
                System.out.println("EBTer base");
                int pc = logic.HexToDEcimalConvert(basereg) + 3;
                int ta = logic.HexToDEcimalConvert(logic.mapSymbolTable.get(operand));
                disp = logic.DecimalToHexa(Math.abs(ta - pc));
            }


////////////////////////////////////



         objCode=mapOpcodeToObjectcode.get(opcode.toUpperCase())+" "+n+i+x+b+p+e+" "+disp;
            System.out.println(objCode);
            String objCode1=logic.DecimalToHexa(logic.HexToDEcimalConvert(mapOpcodeToObjectcode.get(opcode.toUpperCase())));
            if(objCode1=="")objCode1="00";
             String  objCode2=n+i;
             int decimal=Integer.parseInt(objCode2,2);
             objCode1=logic.DecimalToHexa(logic.HexToDEcimalConvert(objCode1)+decimal);
            //String objCode2=logic.DecimalToHexa(logic.HexToDEcimalConvert(x)+logic.HexToDEcimalConvert(b)+logic.HexToDEcimalConvert(p)+logic.HexToDEcimalConvert(e));
             objCode2=x+b+p+e;
              decimal=Integer.parseInt(objCode2,2);
             objCode2=logic.DecimalToHexa(decimal);
             if(objCode2=="")objCode2="0";
            objCode=objCode1+objCode2+disp;
            if(objCode.length()==5)objCode="0"+objCode;

        }


            System.out.println(objCode);

        }



    public static boolean isNumeric(String s) {
        if (s == null || s.equals("")) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    protected void writeHeaderObjectFile() throws IOException {
       // BufferedWriter writer = new BufferedWriter(new FileWriter("object file.txt"));
        //header rec.
        writer.write("H");
        writer.write(label+" ^");
        if(fisrtaddress.length()!=6){
            if (fisrtaddress.length()==4)fisrtaddress="00"+fisrtaddress;
            else if (fisrtaddress.length()==5)fisrtaddress="0"+fisrtaddress;
        }
        writer.write(fisrtaddress+"^");

        int l=Math.abs(logic.startAddress-logic.HexToDEcimalConvert(fisrtaddress));
        String length=logic.DecimalToHexa(l);
        if(length.length()!=6){
            if (length.length()==3) length="000"+length;
            else if (length.length()==4) length="00"+length;
            else if (length.length()==5) length="0"+length;
        }
        writer.write(length);


    }

    protected void writeTextObjectFile() throws IOException {
        if(!objCode.equals("--"))
        //writer.append(objCode+" ");
        textstring=textstring+objCode;
        prevobjCode=objCode;
    }


    protected void writenewTextObjectFile() throws IOException {
       // if(!objCode.equals("--")){
          writer.newLine();
          writer.append("T");
          if(textaddress.length()!=6){
                if (textaddress.length()==4)textaddress="00"+textaddress;
                else if (textaddress.length()==5)textaddress="0"+textaddress;
            }

            writer.append(textaddress+"^"+logic.DecimalToHexa(textstring.length()/2)+"^");
        //}
        writer.append(textstring+literalstring);
      //  writer.append(textstring);
        textaddress=logic.DecimalToHexa(logic.HexToDEcimalConvert(textaddress)+(textstring.length()/2));
        textstring="";
        literalstring="";
    }
    protected void writeEndObjectFile() throws IOException {
        writer.newLine();
        writer.append("E");
        if(fisrtaddress.length()!=6){
            if (fisrtaddress.length()==4)fisrtaddress="00"+fisrtaddress;
            else if (fisrtaddress.length()==5)fisrtaddress="0"+fisrtaddress;
        }
        writer.append(fisrtaddress);
    }

}

