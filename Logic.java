package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;

public class Logic {

    BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
    BufferedWriter appender = new BufferedWriter(new FileWriter("output.txt", true));
    BufferedWriter symTableWriter = new BufferedWriter(new FileWriter("symbolTable.txt"));
    BufferedWriter symTableAppender = new BufferedWriter(new FileWriter("symbolTable.txt", true));

    String[] inputArray = new String[90];
    boolean isLabel = false;
    String printLine = "";
    String errorLine="";
    int error=0;
    String errorStr = "";
    String oldAddressInHexa= "";
    HashMap<Integer,String> ErrorLines=new HashMap<>();
    HashMap<String, Integer> mapOpcode = new HashMap<>();
    HashMap<String, Integer> mapDirective = new HashMap<>();
    HashMap<String, String> mapLabel = new HashMap<>();
    HashMap<String, Integer> mapOpcode4 = new HashMap<>();
    HashMap<String, String> mapRegisters = new HashMap<>();
    HashMap<String, String> mapSymbolTable = new HashMap<>();

    int inputCounter = 0;
    int startAddress = 0;
    int oldAddress = 0;
    String LABEL = "", firstOpernad = "", secondOperand = "", size = "", startLabel = "", opCode = "", comment = "", fullCurrentLine = "";
    boolean isFirstTime = true;

    String errorCheck = "";
    int errorCheckIndex = 0, printIndex = 0;
    String[] errorCheckArray = new String[90];



    public Logic() throws IOException {
    }

    void start(int type) throws FileNotFoundException, IOException
    {
        generatingOpcode();
        File file = new File("input.txt");

        // Fixed format file
        if(type == 0)
        {
            file = new File("inputFixedFormat.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        String readWord;
        while ((readWord = br.readLine()) != null) {
            inputArray[inputCounter++] = readWord;
        }
        readInput(type);
    }

    void readInput(int type) throws IOException {
        for(int i = 0; i < inputCounter; i++)
        {
            String currentLine = inputArray[i];

            // Check free format
            if(type == 0)
            {
                fullCurrentLine = currentLine;
                errorCheck = checkFixedFormate();
                if(errorCheckArray[errorCheckIndex] == null)
                    errorCheckArray[errorCheckIndex] = errorCheck;
                //
                System.out.println("type 0 error"+errorCheckArray[errorCheckIndex]);
                errorCheckIndex ++;
            }

            currentLine = currentLine.trim();
            int check = checkOpcode(currentLine,0,i);
            if(check == 22)
            {
                directiveOperation(currentLine,i);
            }
        }
        isFirstTime = false;
        errorCheckIndex = 0;
        for(int i = 0; i < inputCounter; i++)
        {

            String currentLine = inputArray[i];
            currentLine = currentLine.trim();

            if(currentLine.length() == 0)
                continue;
            if(currentLine.charAt(0) == '.'){
                comment = currentLine;
            }
            else {
                if(i == 0)
                {
                    writer.write("Line no.          Address          Label          Opcode          Operands                  Comment");
                    writer.newLine();
                    checkFirstLine(currentLine);
                    writer.close();
                }
                else if(i == inputCounter-1)
                {
                    checkLastLine(currentLine);
                }
                else
                {
                    checkCode(currentLine,i);
                }
            }
            printLine = (i + 1) + "";
            addSymbolTable();
            printOutput(i);

            errorCheckIndex ++;

        }
        if(ErrorLines.containsKey(90))
        {
            appender.newLine();
            appender.append("                      ***"+ErrorLines.get(90));
        }
        printSymbolTable();
        appender.close();
    }

    private void addSymbolTable() throws IOException {
        String symboleTableLine = "";
        String startAddressInHexa = DecimalToHexa(oldAddress);
        if(isLabel)
        {
            if(!mapSymbolTable.containsKey(LABEL.toUpperCase()))
                mapSymbolTable.put(LABEL,startAddressInHexa);

        }
//        else if(mapLabel.containsKey(firstOpernad.toUpperCase()))
//        {
//            mapSymbolTable.put(firstOpernad,startAddressInHexa);
//        }
    }

    private void printSymbolTable() throws IOException {
        String symboleTableLine = "";
        symTableWriter.write("Lable          Address");
        symTableWriter.newLine();
        symTableWriter.close();

        for (Entry<String, String> entry : mapSymbolTable.entrySet()) {
            symTableAppender.newLine();
            symTableAppender.append(entry.getKey()+"             "+entry.getValue());
        }

        symTableAppender.newLine();
        symTableAppender.append(symboleTableLine);
        symTableAppender.close();
    }

    private String checkFixedFormate() {

        for(int i = fullCurrentLine.length()-1; i<65;i++)
            fullCurrentLine +=" ";

        boolean spaceCheck = false;
        int j = 0;

        System.out.println(fullCurrentLine);

        // Label is Fixed
        for(; j < 7; j++)
        {
            if(fullCurrentLine.charAt(j) == ' ')
            {
                spaceCheck = true;
            }
            if(spaceCheck == true && fullCurrentLine.charAt(j) != ' ')
            {
                System.out.println("Fixed Formate error at " + (j+1));
                return "                   **** Fixed Formate error";
            }
        }
        spaceCheck = false;

        // Blank at 9
        if(fullCurrentLine.charAt(8) != ' ')
        {
            System.out.println("Fixed Formate error at 9");
            return "                   **** Fixed Formate error";
        }

        // Opcode is Fixed
        j = 9;
        for(; j < 14; j++)
        {
            if(fullCurrentLine.charAt(j) == ' ')
                spaceCheck = true;
            if(spaceCheck == true && fullCurrentLine.charAt(j) != ' ')
            {
                System.out.println("Fixed Formate error at " + (j+1));
                return "                   **** Fixed Formate error";
            }
        }
        spaceCheck = false;

        // Blank at 15-16
        if(fullCurrentLine.charAt(15) != ' ' && fullCurrentLine.charAt(16) != ' ')
        {
            System.out.println("Fixed Formate error at 15-16");
            return "                   **** Fixed Formate error";
        }

        // Operand is Fixed
        j = 17;
        for(; j < 34; j++)
        {
            if(fullCurrentLine.charAt(j) == ' ')
                spaceCheck = true;
            if(spaceCheck == true && fullCurrentLine.charAt(j) != ' ')
            {
                System.out.println("Fixed Formate error at " + (j+1));
                return "                   **** Fixed Formate error";
            }
        }
        spaceCheck = false;

        // Comment is Fixed
        j = 35;
        for(; j < 65; j++)
        {
            if(fullCurrentLine.charAt(j) == ' ')
                spaceCheck = true;
            if(spaceCheck == true && fullCurrentLine.charAt(j) != ' ')
            {
                System.out.println("Fixed Formate error at " + (j+1));
                return "                   **** Fixed Formate error";
            }
        }
        return null;
    }

    private void printOutput(int LINENUMBER) throws IOException {


        if(opCode.toUpperCase().equals("END"))
        {
            firstOpernad = LABEL.toUpperCase();
            LABEL = "";
        }

        // Check + - / *
        String dividedOperand1 = "";
        String dividedOperand2 = "";
        boolean singFound = false;
        for(int i = 0; i <firstOpernad.length(); i++)
        {
            if(firstOpernad.charAt(i) == '+' || firstOpernad.charAt(i) == '-' || firstOpernad.charAt(i) == '*' || firstOpernad.charAt(i) == '/')
            {
                if((errorCheckArray[printIndex] != null && errorCheckArray[printIndex].equals("        *** Error in Instruction format")))
                    errorCheckArray[printIndex] = null;
                i++;
            }

            if(singFound == false)
                dividedOperand1 += firstOpernad.charAt(i);
            else
                dividedOperand2 += firstOpernad.charAt(i);
        }

        // Number
        int neededSpaces = 18 - printLine.length();
        for(int j = 0; j < neededSpaces; j++){
            printLine = printLine + ' ';
        }

        // Address
        String startAddressInHexa = DecimalToHexa(startAddress);
        oldAddressInHexa = DecimalToHexa(oldAddress);
        printLine = printLine + oldAddressInHexa;
        neededSpaces = 17 - oldAddressInHexa.length();
        for(int j = 0; j < neededSpaces; j++){
            printLine = printLine + ' ';
        }
        if(errorCheckArray[printIndex] == null)
            oldAddress = startAddress;

        // Label
        printLine = printLine + LABEL.toUpperCase();
        neededSpaces = 15 - LABEL.length();
        for(int j = 0; j < neededSpaces; j++){
            printLine = printLine + ' ';
        }

        // Opcode
        printLine = printLine + opCode.toUpperCase() ;
        neededSpaces = 16 - opCode.length();
        for(int j = 0; j < neededSpaces; j++){
            printLine = printLine + ' ';
        }

        // Operand if opcode is directive
        if(isLabel == true){
            printLine = printLine + size;
            isLabel = false;
            neededSpaces = 29 - size.length();
            for(int j = 0; j < neededSpaces; j++){
                printLine = printLine + ' ';
            }
        }

        // Operand if opcode is operation
        else if(secondOperand.equals("")){

            printLine = printLine + firstOpernad;
            neededSpaces = 29 - firstOpernad.length();
            for(int j = 0; j < neededSpaces; j++){
                printLine = printLine + ' ';
            }
        }
        else{
            printLine = printLine + firstOpernad;
            printLine = printLine + "," + secondOperand;
            neededSpaces = 29 - (1 + firstOpernad.length() + secondOperand.length());
            for(int j = 0; j < neededSpaces; j++){
                printLine = printLine + ' ';
            }
        }


        printLine = printLine + comment;
        size = firstOpernad = secondOperand = opCode = comment = LABEL = "";
        appender.newLine();
        appender.append(printLine);
//        if(ErrorLines.containsKey(LINENUMBER))
//        {
//            errorLine=errorLine+"                      ***"+ErrorLines.get(LINENUMBER);
//            appender.newLine();
//            appender.append(errorLine);
//            errorLine="";
//        }
//
//
//
//        if(error != 0)
//        {
//            appender.newLine();
//            appender.append(errorStr);
//            error = 0;
//            errorStr = "";
//        }

        if(errorCheckArray[printIndex] != null)
        {
            appender.newLine();
            appender.append(errorCheckArray[printIndex]);
            errorCheck = "";
        }
        printIndex++;

        printLine = "";
    }

    private void checkFirstLine(String currentLine) {
        currentLine = currentLine.trim();
        int spaceCheck = 0;
        int indexOfFirstLine = 0;
        while(spaceCheck <= 2)
        {
            if(!Character.isLetter(currentLine.charAt(0)))
            {
                System.out.println("Wrong Label format");
                ErrorLines.put(0,"Wrong Label Format!");
                errorCheckArray[errorCheckIndex] = "        *** Wrong Label Format!";
                return;
            }
            if(spaceCheck == 0) //Reading the LABEL
            {
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) != ' ')
                {
                    startLabel = startLabel + currentLine.charAt(indexOfFirstLine);
                    indexOfFirstLine++;
                }
                LABEL = startLabel;
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) == ' ')
                    indexOfFirstLine++;
                if(indexOfFirstLine == currentLine.length())
                {
                    //START IS NOT GIVEN IN THE INOUT
                    System.out.println("missing or misplaced operation mnemonic");
                    errorCheckArray[errorCheckIndex] = "        *** missing or misplaced operation mnemonic";
                    break;
                }
                else
                    spaceCheck++;
            }
            if(spaceCheck == 1) //checking that start word is
            {
                String readStartWord = "";
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) != ' ')
                {
                    readStartWord = readStartWord + currentLine.charAt(indexOfFirstLine);
                    indexOfFirstLine++;
                }
                opCode = readStartWord;
                if(indexOfFirstLine == currentLine.length())
                {
                    //ADDRESS IS NOT GIVEN IN THE INOUT
                    System.out.println("missing or misplaced operation mnemonic");
                    ErrorLines.put(0,"missing or misplaced operation mnemonic");
                    errorCheckArray[errorCheckIndex] = "        *** missing or misplaced operation mnemonic";
                    break;
                }
                if(!readStartWord.toUpperCase().equals("START"))
                {
                    //start word is not given in the input file correctly
                    System.out.println("start word is not given in the input file correctly");
                    ErrorLines.put(0,"Start word is not given in the input file correctly!");
                    errorCheckArray[errorCheckIndex] = "        *** Start word is not given in the input file correctly!";
                }
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) == ' ')
                    indexOfFirstLine++;
                spaceCheck++;
            }
            if(spaceCheck == 2) //Reading start address from first line
            {
                String readHexAddress = "";
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) != ' ')
                {
                    readHexAddress = readHexAddress + currentLine.charAt(indexOfFirstLine);
                    indexOfFirstLine++;
                }
                if(indexOfFirstLine != currentLine.length())
                {
                    //Error found, extra word after getting the start address.
                    System.out.println("Error found, extra word after getting the start address.");
                    ErrorLines.put(0,"Error found, extra word after getting the start address!");
                    errorCheckArray[errorCheckIndex] = "        *** Error found, extra word after getting the start address!";
                    break;
                }
                for(int k = 0; k <  readHexAddress.length(); k++)
                {
                    char aux = readHexAddress.charAt(k);
                    if(!(aux >= '0' && aux <= '9'))
                        if(!((aux >= 'a' && aux <= 'f') || (aux >= 'A' && aux <= 'F')))
                        {
                            System.out.println("ERROR, not in HEX Format range");
                            ErrorLines.put(0,"ERROR address of start is not given in HEX Formate range!");
                            errorCheckArray[errorCheckIndex] = "        *** ERROR address of start is not given in HEX Formate range!";
                            return;
                        }
                }
                if(readHexAddress.length() > 4)
                {
                    //ERROR, OUT Of Hex range
                    System.out.println("ERROR, OUT Of  range");
                    ErrorLines.put(0,"Error , out of range!");
                    errorCheckArray[errorCheckIndex] = "        *** ERROR Out of range!";
                    break;
                }
                startAddress = HexToDEcimalConvert(readHexAddress);
                oldAddress = HexToDEcimalConvert(readHexAddress);
                isLabel = true;
                size = readHexAddress;
                break;
            }
        }
    }

    private void checkLastLine(String currentLine) {
        currentLine = currentLine.trim();
        int spaceCheck = 0;
        int indexOfFirstLine = 0;
        while(spaceCheck < 2)
        {
            String end = "";
            String endLabel = "";
            if(spaceCheck == 0) //Reading the LABEL
            {
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) != ' ')
                {
                    end = end + currentLine.charAt(indexOfFirstLine);
                    indexOfFirstLine++;
                }
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) == ' ')
                    indexOfFirstLine++;
                if(indexOfFirstLine == currentLine.length())
                {
                    //END IS NOT GIVEN IN THE INOUT
                    System.out.println("missing or misplaced operation mnemonic");
                    ErrorLines.put(90,"Error missing or misplaced operation mnemonic ");
                    errorCheckArray[errorCheckIndex] = "        *** Error missing or misplaced operation mnemonic";
                    break;
                }
                if(!end.toUpperCase().equals("END"))
                {
                    //start word is not given in the input file correctly
                    System.out.println("end word is not given in the input file correctly");
                    ErrorLines.put(90,"End word is not given in the input file correctly!");
                    errorCheckArray[errorCheckIndex] = "        *** End word is not given in the input file correctly!";

                    break;
                }
                else
                    spaceCheck++;
            }
            if(spaceCheck == 1) //checking that start word is
            {
                String readStartWord = "";
                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) != ' ')
                {
                    endLabel = endLabel + currentLine.charAt(indexOfFirstLine);
                    indexOfFirstLine++;
                }
                if(!endLabel.toUpperCase().equals(startLabel.toUpperCase()))
                {
                    //start word is not given in the input file cprrectly
                    System.out.println("end label is not given in the input file correctly ");
                    ErrorLines.put(90,"Start label must match the end Label!");
                    errorCheckArray[errorCheckIndex] = "        *** Start label must match the end Label!";
                    break;
                }

                while(indexOfFirstLine < currentLine.length() && currentLine.charAt(indexOfFirstLine) == ' ')
                    indexOfFirstLine++;
                spaceCheck++;

                if(indexOfFirstLine != currentLine.length())
                {
                    //ADDRESS IS NOT GIVEN IN THE INOUT
                    System.out.println("missing or misplaced operation mnemonic");
                    ErrorLines.put(90,"OverFlow in the instruction operands!");
                    errorCheckArray[errorCheckIndex] = "        *** OverFlow in the instruction operands!";
                    break;
                }
                opCode = "END";
                LABEL = startLabel;

            }
        }
    }

    private void checkCode(String currentLine,int lineNumber)
    {
        int check = checkOpcode(currentLine , 1,lineNumber);
        if(check == 21) //Normal operation, so only opcode present and no label and operands for the opcode presnet
        {
            normalOperation(currentLine,0,lineNumber);
            //System.out.println(firstOperanad + " " + secondOperand);
        }
        else if(check == 23)//Normal with label
        {
            normalOperation(currentLine,1,lineNumber);
        }
        else if(check == 22)
        {
            directiveOperation(currentLine,lineNumber);
        }
        else if(check == 8)
        {
            return;
        }
    }

    private int checkOpcode(String currentLine,int checker,int linenumber)
    {
        int spaceCheck = 0;
        int checkOpcode = 0;
        int indexOfLine = 0;
        Boolean isNormalOperation1 = false, isNormalOperation2 = false, isDirective = false;
        String firstWord = "", secondWord = "";
        //System.out.println(currentLine + " " + indexOfFirstLine + " spacechek: " + spaceCheck);
        if(spaceCheck == 0) //Reading the first word
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                firstWord = firstWord + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
                indexOfLine++;
            spaceCheck++;
            if(mapOpcode.containsKey(firstWord.toUpperCase()) || mapOpcode4.containsKey(firstWord.toUpperCase()))
            {
                //System.out.println("First word");
                checkOpcode++; // the value of it indicates if there is a label or not
                isNormalOperation1 = true; // isNormalOperation 1 indicates that there is no label
            }
        }
        if(spaceCheck == 1) //Reading the second word
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                secondWord = secondWord + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
                indexOfLine++;
            spaceCheck++;
            if(mapDirective.containsKey(secondWord.toUpperCase()))
            {
                //System.out.println("Second word");
                checkOpcode++;
                isDirective = true;
            }
            else if(mapOpcode.containsKey(secondWord.toUpperCase()) || mapOpcode4.containsKey(secondWord.toUpperCase()))
            {
                //System.out.println("First word");
                checkOpcode++;
                isNormalOperation2 = true;
            }
        }
        if(checkOpcode != 1 && checker != 0) //No any opcode
        {
            //'unrecognized operation code
            //System.out.println("number of opcode error"+firstWord+" "+secondWord+" "+checkOpcode);
            ErrorLines.put(linenumber,"number of opcode error");
            System.out.println("number of opcode error");
            errorCheckArray[errorCheckIndex] = "        *** number of opcode error";
            return 8;
        }


        if(isNormalOperation1)
            return 21; //Normal operation, so only opcode present and no label
        else if(isDirective)
            return 22; // Directive operation
        else if(isNormalOperation2)
            return 23; // There is a label and there is an mnemonic
        else
            return 0;
    }

    private int normalOperation(String currentLine,int labelChecker,int lineNUmber)
    {
        int spaceCheck = 0;
        int indexOfLine = 0;
        int numberOfOperands = 0;
        firstOpernad = "";
        secondOperand = "";
        opCode = "";
        boolean isCommaFound = false;
        //System.out.println(currentLine + " " + indexOfFirstLine + " spacechek: " + spaceCheck);
        if(labelChecker == 1)
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                LABEL = LABEL + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            mapLabel.put(LABEL,"0");
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
                indexOfLine++;
        }
        if(spaceCheck == 0) //Reading the opCode
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                opCode = opCode + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            System.out.println(opCode);
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
                indexOfLine++;
            spaceCheck++;
            if(!Character.isLetter(currentLine.charAt(0)))
            {
                if(currentLine.charAt(0) != '+')
                {
                    //Error, for Format 4, it must be + but the given is another character
                    ErrorLines.put(lineNUmber,"Error missing or misplaced operation mnemonic ");
                    errorCheckArray[errorCheckIndex] = "        *** Error missing or misplaced operation mnemonic";
                }
                else
                {
                    numberOfOperands = mapOpcode4.get(opCode.toUpperCase());
                }
            }
            else
            {//System.out.println(opCode);
                if(opCode.charAt(0)=='+')
                    numberOfOperands = mapOpcode4.get(opCode.toUpperCase());
                else
                    numberOfOperands = mapOpcode.get(opCode.toUpperCase());
            }
            if(indexOfLine == currentLine.length())
            {
                //No operands are given
                ErrorLines.put(lineNUmber,"No Operands are given! ");
                errorCheckArray[errorCheckIndex] = "        *** Error missing or misplaced operation mnemonic";
                //return;
            }
        }
        if(numberOfOperands == 1)
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                firstOpernad = firstOpernad + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            while (indexOfLine < currentLine.length())
                indexOfLine++;
            if(indexOfLine != currentLine.length())
            {
                //Error in operands

                ErrorLines.put(lineNUmber,"Error in operands! OverFlow!");
                errorCheckArray[errorCheckIndex] = "        *** Error in operands! OverFlow!";
                return 3;
            }
        }
        else
        {
            while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
            {
                if(Character.isLetter(currentLine.charAt(indexOfLine)))
                {
                    firstOpernad = firstOpernad + currentLine.charAt(indexOfLine);
                    indexOfLine++;
                }
                else if(currentLine.charAt(indexOfLine) == ',')
                {
                    indexOfLine++;
                    isCommaFound = true;
                    while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
                        indexOfLine++;
                    while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
                    {
                        secondOperand = secondOperand + currentLine.charAt(indexOfLine);
                        indexOfLine++;
                    }
                }
            }
            if(!isCommaFound)
            {
                while(indexOfLine < currentLine.length() && (currentLine.charAt(indexOfLine) == ' ' || currentLine.charAt(indexOfLine) == ','))
                {
                    if(currentLine.charAt(indexOfLine) == ',')
                    {
                        isCommaFound = true;
                    }
                    indexOfLine++;
                }
                if(!isCommaFound)
                {
                    ErrorLines.put(lineNUmber,"No Comma Found!");
                    //Error in getting the second operand
                    errorCheckArray[errorCheckIndex] = "        *** No Comma Found!";
                    return 3;
                }
                while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
                {
                    secondOperand = secondOperand + currentLine.charAt(indexOfLine);
                    indexOfLine++;
                }
            }
            if(indexOfLine != currentLine.length())
            {
                //Error extra words aw ay 7aga
                ErrorLines.put(lineNUmber,"Extra Words found !");
                errorCheckArray[errorCheckIndex] = "        *** Extra Words found !";
                return 3;
            }
        }
        String abc = opCode.toUpperCase();
        if (abc.equals("LDA") || abc.equals("LDX")||  abc.equals("LDS") || abc.equals("LDT") || abc.equals("STA") || abc.equals("STS"))
        {
            //System.out.println("HERE " + opCode);
            String aux = opCode.substring(0, opCode.length() - 1);
            if(aux.toUpperCase().equals("ST"))
            {
                System.out.println(opCode);
                String tryy = opCode.charAt(2) + "";
                tryy = tryy.toUpperCase();
                if(!mapRegisters.containsKey(tryy)) //Given register to store at is invalid
                {
                    //Error
                    ErrorLines.put(lineNUmber,"Given registers to store at is invalid!");
                    errorCheckArray[errorCheckIndex] = "        *** Given registers to store at is invalid!";
                    return -1;
                }
                boolean done = true;
                int startIndex;
                if(firstOpernad.charAt(0) == '+')
                    startIndex = 1;
                else
                    startIndex = 0;
                if(firstOpernad.charAt(startIndex) == '#' || firstOpernad.charAt(startIndex) == '@') // Immediate case or indirect case
                {
                    if(!Character.isLetter(firstOpernad.charAt(startIndex + 1)))//So this is not a LABEL
                    {
                        for(int i = startIndex + 1; i < firstOpernad.length(); i++)
                        {
                            if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9' ||
                                    firstOpernad.charAt(i) >= 'a' && firstOpernad.charAt(i) <= 'f'||
                                    firstOpernad.charAt(i) >= 'A' && firstOpernad.charAt(i) <= 'F'))
                            {
                                //Error, address not give in decimal numbers
                                //errorCheckArray[errorCheckIndex] = "        *** (Check)Error, address not give in decimal numbers!";
                                //System.out.println("Error");
                                done = false;
                                break;
                            }
                        }
                    }
                    else if(!mapLabel.containsKey(firstOpernad.substring(startIndex + 1, firstOpernad.length()))) //Given Label is not listed in mapLabel, so wrong input
                    {
                        ErrorLines.put(lineNUmber,"Given Label is not listed in mapLabel, so wrong input");
                        errorCheckArray[errorCheckIndex] = "        ***Given Label is not listed in mapLabel, so wrong input";
                        done = false;
                    }
                }
                else
                {
                    for(int i = startIndex; i < firstOpernad.length(); i++)//Neither immediate nor indirect
                    {
                        if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9'))
                        {
                            //Error, address not given in decimal numbers
                            ///System.out.println("Error");
                            // errorCheckArray[errorCheckIndex] = "        *** (Check) Error, address not given in decimal numbers";
                            done = false;
                            break;
                        }
                    }
                    if(mapLabel.containsKey(firstOpernad))
                        done = true;
                }
                if(!done)
                {
                    ErrorLines.put(lineNUmber,"Error in load instruction!");
                    errorCheckArray[errorCheckIndex] = "        *** Error in load instruction";
                }
                else
                {
                    if(startIndex == 1)
                        startAddress += 4;
                    else
                        startAddress += 3;
                }
                return 55;
            }
            else if(aux.toUpperCase().equals("LD"))
            {
                String tryy = opCode.charAt(2) + "";
                tryy = tryy.toUpperCase();
                if(!mapRegisters.containsKey(tryy)) //Given register to store at is invalid
                {
                    //Error
                    ErrorLines.put(lineNUmber,"Given registers to store at is invalid!");
                    errorCheckArray[errorCheckIndex] = "        *** Error: Given registers to store at is invalid!";
                    return -1;
                }
                int startIndex = 0;
                if(firstOpernad.charAt(0) == '+')
                    startIndex = 1;
                boolean done = true, doneIfLiteral = false;
                if(firstOpernad.charAt(startIndex) == '#' || firstOpernad.charAt(startIndex) == '@') // Immediate case or indirect case
                {
                    if(!Character.isLetter(firstOpernad.charAt(startIndex + 1)))//So this is not a LABEL
                    {
                        for(int i = startIndex + 1; i < firstOpernad.length(); i++)
                        {
                            if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9' ||
                                    firstOpernad.charAt(i) >= 'a' && firstOpernad.charAt(i) <= 'f'||
                                    firstOpernad.charAt(i) >= 'A' && firstOpernad.charAt(i) <= 'F'))
                            {
                                //Error, address not give in decimal numbers
                                //errorCheckArray[errorCheckIndex] = "        *** (Check)Error, address not give in decimal numbers!";
                                //System.out.println("Error");
                                done = false;
                                break;
                            }
                        }
                    }
                    else if(!mapLabel.containsKey(firstOpernad.substring(startIndex + 1, firstOpernad.length()))) //Given Label is not listed in mapLabel, so wrong input
                    {
                        ErrorLines.put(lineNUmber,"Given Label is not listed in mapLabel, so wrong input");
                        errorCheckArray[errorCheckIndex] = "        ***Given Label is not listed in mapLabel, so wrong input";
                        done = false;
                    }
                }
                else
                {
                    boolean isLiteral = false;
                    for(int i = 0; i < firstOpernad.length(); i++)
                    {
                        if(firstOpernad.charAt(i) == '=')
                        {
                            isLiteral = true;
                            break;
                        }
                    }
                    if(isLiteral)
                    {
                        done = false;
                        String literalStrinng = ""; // For getting the Literal string as total and removing spaces
                        String valueOfTheLiteral = "";
                        int valueInDecimalIfLiteralIsX;
                        for(int i = 0; i < firstOpernad.length(); i++)
                        {
                            if(firstOpernad.charAt(i) != ' ' && firstOpernad.charAt(i) != '+')
                                literalStrinng += firstOpernad.charAt(i);
                        }
                        //Checking if either W, C or X and that its correct format having the ' ' placed correctly
                        if(literalStrinng.charAt(1) == 'W' || literalStrinng.charAt(1) == 'w' ||
                                literalStrinng.charAt(1) == 'X' || literalStrinng.charAt(1) == 'x' ||
                                literalStrinng.charAt(1) == 'C' || literalStrinng.charAt(1) == 'c')
                        {
                            if(literalStrinng.charAt(2) == '\'' && literalStrinng.charAt(literalStrinng.length() - 1) == '\'')
                            {
                                if(literalStrinng.charAt(1) == 'W' || literalStrinng.charAt(1) == 'w')
                                {
                                    if(literalStrinng.substring(3, literalStrinng.length() - 1).length() <= 4)
                                    {
                                        boolean ok = true;
                                        for (int i = 3; i < literalStrinng.length() - 1; i++)
                                        {
                                            if(!Character.isDigit(literalStrinng.charAt(i)))
                                            {
                                                ok = false;
                                                break;
                                            }
                                        }
                                        if(ok)
                                        {
                                            valueOfTheLiteral = literalStrinng.substring(3, literalStrinng.length() - 1);
                                            mapRegisters.put(opCode.charAt(2) + "", valueOfTheLiteral);
                                            done = true;
                                        }
                                    }
                                }
                                else if(literalStrinng.charAt(1) == 'X' || literalStrinng.charAt(1) == 'x')
                                {
                                    if(literalStrinng.substring(3, literalStrinng.length() - 1).length() == 6)
                                    {
                                        boolean ok = true;
                                        for (int i = 3; i < literalStrinng.length() - 1; i++)
                                        {
                                            if(!Character.isDigit(literalStrinng.charAt(i)))
                                            {
                                                if(!(literalStrinng.charAt(i) >= 'a' && literalStrinng.charAt(i) <= 'f'))
                                                {
                                                    if(!(literalStrinng.charAt(i) >= 'A' && literalStrinng.charAt(i) <= 'F'))
                                                    {
                                                        ok = false;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if(ok)
                                        {
                                            valueOfTheLiteral = literalStrinng.substring(3, literalStrinng.length() - 1);
                                            valueInDecimalIfLiteralIsX = HexToDEcimalConvert(valueOfTheLiteral);
                                            String auxString = Integer.toString(valueInDecimalIfLiteralIsX);
                                            mapRegisters.put(opCode.charAt(2) + "", auxString);
                                            done = true;
                                        }
                                    }
                                }
                                else if(literalStrinng.charAt(1) == 'C' || literalStrinng.charAt(1) == 'c')
                                {
                                    if(literalStrinng.substring(3, literalStrinng.length() - 1).length() == 6)
                                    {
                                        boolean ok = true;
                                        for (int i = 3; i < literalStrinng.length() - 1; i++)
                                        {
                                            if(!Character.isLetter(literalStrinng.charAt(i)))
                                            {
                                                ok = false;
                                                break;
                                            }
                                        }
                                        if (ok)
                                        {
                                            valueOfTheLiteral = literalStrinng.substring(3, literalStrinng.length() - 1);
                                            mapRegisters.put(opCode.charAt(2) + "", valueOfTheLiteral);
                                            done = true;
                                        }
                                    }
                                }
                            }
                        }
                        String kk = opCode.charAt(2) + "";
                        kk = kk.toUpperCase();
                        System.out.println("AMR AKMAAAL " + mapRegisters.get(kk));
                    }
                    for(int i = 0; !isLiteral && i <  firstOpernad.length(); i++)//Neither immediate nor indirect
                    {
                        if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9'))
                        {
                            //Error, address not given in decimal numbers
                            ///System.out.println("Error");
                            //errorCheckArray[errorCheckIndex] = "        *** (Check) Error, address not given in decimal numbers";
                            done = false;
                            break;
                        }
                    }
                    if(mapLabel.containsKey(firstOpernad.toUpperCase()))
                        done = true;
                }
                if(!done)
                {
                    ErrorLines.put(lineNUmber,"Error in load instruction!");
                    errorCheckArray[errorCheckIndex] = "        *** Error in load instruction";
                }
                else
                {
                    if(startIndex == 1)
                        startAddress += 4;
                    else
                        startAddress += 3;
                }
            }
            return 55;
        }
        boolean done1 = false;
        if(opCode.toUpperCase().equals("TIXR") || opCode.toUpperCase().equals("RMO") || opCode.toUpperCase().equals("SUBR") || opCode.toUpperCase().equals("COMPR")){
            startAddress += 2;
            done1 = true;
        }
        if(!Character.isLetter(opCode.charAt(0)))
        {
            if(opCode.charAt(0) == '+')
                startAddress += 4;
            else
            {
                System.out.println("Error in format 4");
                ErrorLines.put(lineNUmber,"Error in format 4");
                errorCheckArray[errorCheckIndex] = "        *** Error in format 4";
                return -1;
            }
        }
        else if(!done1)
        {
            startAddress += 3;
            done1 = true;
        }
        if(secondOperand.length() != 0)
        {
            if(secondOperand.charAt(0) == '#') //Error
            {
                ErrorLines.put(lineNUmber,"Immediate cannot be destination!");
                errorCheckArray[errorCheckIndex] = "        *** Immediate cannot be destination!";
            }
        }
        if(numberOfOperands == 2)
        {
            if(!(mapRegisters.containsKey(firstOpernad.toUpperCase()) && mapRegisters.containsKey(secondOperand.toUpperCase())))
            {
                //Error,this is an instruction that needs two registers and operands, but they are given wrong in input
                ErrorLines.put(lineNUmber,"Error,this is an instruction that needs two registers and operands, but they are given wrong in input");
                errorCheckArray[errorCheckIndex] = "        *** Error,this is an instruction that needs two registers and operands, but they are given wrong in input";
            }
            if(secondOperand.charAt(0) == '#')
            {
                //Error, immediate can not be a destination
                ErrorLines.put(lineNUmber,"Immediate cannot be destination!");
                errorCheckArray[errorCheckIndex] = "        *** Immediate cannot be destination!";

                return -1;
            }
        }
        if(opCode.toUpperCase().equals("TIXR"))
        {
            if(!mapRegisters.containsKey(firstOpernad.toUpperCase()))
            {
                //Error, TIXR takes register only as an operand for itbut the given input is not register
                ErrorLines.put(lineNUmber,"Error, TIXR takes register only as an operand for it but the given input is not register");
                errorCheckArray[errorCheckIndex] = "        *** Error, TIXR takes register only as an operand for it but the given input is not register";
            }
        }
        if(numberOfOperands == 1)
        {
            boolean done = true;
            if(firstOpernad.charAt(0) == '#' || firstOpernad.charAt(0) == '@') // Immediate case or indirect case
            {
                if(!Character.isLetter(firstOpernad.charAt(1)))//So this is not a LABEL
                {
                    for(int i = 1; i < firstOpernad.length(); i++)
                    {
                        if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9' ||
                                firstOpernad.charAt(i) >= 'a' && firstOpernad.charAt(i) <= 'f'||
                                firstOpernad.charAt(i) >= 'A' && firstOpernad.charAt(i) <= 'F'))
                        {
                            //Error, address not give in decimal numbers
                            //System.out.println("Error");
                            //errorCheckArray[errorCheckIndex] = "        *** (Check)Error, address not give in decimal numbers";

                            done = false;
                            break;
                        }
                    }
                }
                else if(!mapLabel.containsKey(firstOpernad.substring(1, firstOpernad.length()))) //Given Label is not listed in mapLabel, so wrong input
                {
                    ErrorLines.put(lineNUmber,"Given label is not listed in map Label!");
                    errorCheckArray[errorCheckIndex] = "        *** Given label is not listed in map Label!";
                    done = false;
                }
            }
            else
            {
                for(int i = 0; i < firstOpernad.length(); i++)//Neither immediate nor indirect
                {
                    if(!(firstOpernad.charAt(i) >= '0' && firstOpernad.charAt(i) <= '9'))
                    {
                        //Error, address not give in decimal numbers
                        ///System.out.println("Error");
                        //errorCheckArray[errorCheckIndex] = "        *** (Check)Error, address not give in decimal numbers";

                        done = false;
                        break;
                    }
                }

                if(mapLabel.containsKey(firstOpernad))
                {
                    done = true;
                }
                if(mapRegisters.containsKey(firstOpernad))
                {
                    done = true;
                }
            }
            if(!done) {
                //System.out.println("Error");
                ErrorLines.put(lineNUmber,"Trace Error in Instruction format");
                errorCheckArray[errorCheckIndex] = "        *** Error in Instruction format";
                System.out.println("Abdoo  "+errorCheckIndex+" "+errorCheckArray[errorCheckIndex]);
            }
        }
        return 0;
    }

    private void directiveOperation(String currentLine,int Linenumber)
    {
        int indexOfLine = 0;
        LABEL = "";
        opCode = "";
        size = "";
        isLabel = true;
        if(!Character.isLetter(currentLine.charAt(0)))
        {

            System.out.println("Wrong Label format");
            ErrorLines.put(Linenumber,"Wrong Label Format!");
            errorCheckArray[errorCheckIndex] = "        *** Wrong Label Format!";
            return;
        }
        while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
        {
            if(Character.isLetter(currentLine.charAt(indexOfLine)) || Character.isDigit(currentLine.charAt(indexOfLine)))
            {
                LABEL = LABEL + currentLine.charAt(indexOfLine);
                indexOfLine++;
            }
            else
            {
                //Error, wrong naming for the Label
                System.out.println("Wrong Label format");
                ErrorLines.put(Linenumber,"Wrong Label Format!");
                errorCheckArray[errorCheckIndex] = "        *** Wrong Label Format!";
                return;
            }
        }

        while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
            indexOfLine++;
        if(indexOfLine == currentLine.length())
        {
            //ERROR
            System.out.println("The directive type and value are not given");
            ErrorLines.put(Linenumber,"The directive type and value are not given");
            errorCheckArray[errorCheckIndex] = "        *** The directive type and value are not given";
            return;
        }
        while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
        {
            opCode = opCode + currentLine.charAt(indexOfLine);
            indexOfLine++;
        }
        while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) == ' ')
            indexOfLine++;
        if(indexOfLine == currentLine.length())
        {
            System.out.println("The directive type and value are not given");
            ErrorLines.put(Linenumber,"The directive type and value are not given");
            errorCheckArray[errorCheckIndex] = "        *** The directive type and value are not given";
            return;
        }
        while(indexOfLine < currentLine.length() && currentLine.charAt(indexOfLine) != ' ')
        {
            size = size + currentLine.charAt(indexOfLine);
            indexOfLine++;
        }
        if(indexOfLine != currentLine.length())
        {
            //ERROR, extra words found after getting the size of the directive which is wrong,
            //beacuse size of directive must be last thing in the line
            System.out.println("Wrong input format");
            ErrorLines.put(Linenumber,"Wrong input format!");
            errorCheckArray[errorCheckIndex] = "        *** Wrong input format!";
            return;
        }
        if(opCode.toUpperCase().equals("RESB")){
            if(isFirstTime)
            {
                if(mapLabel.containsKey(LABEL.toUpperCase())){
                    //Error, LABEL is duplicated
                    System.out.println(LABEL + " is duplicated");
                    ErrorLines.put(Linenumber,LABEL + " is duplicated");
                    errorCheckArray[errorCheckIndex] = "        ***"+ LABEL + " is duplicated";
                    return;
                }
                mapLabel.put(LABEL, size);
                return;
            }
            else
            {
                for(int i = 0; i < size.length(); i++){
                    if(!(size.charAt(i) >= '0' && size.charAt(i) <= '9')){
                        //Error, size is not given in decimal format
                        ErrorLines.put(Linenumber,"Error,size is not given in decimal format!");
                        errorCheckArray[errorCheckIndex] = "Error,size is not given in decimal format!";
                        return;
                    }
                }
                mapLabel.put(LABEL, size + "V") ; //!->size to be reserved in the memory, not a value assigned to that Label
                startAddress += Integer.parseInt(size);
            }
        }
        else if(opCode.toUpperCase().equals("RESW")){
            if(isFirstTime)
            {
                if(mapLabel.containsKey(LABEL.toUpperCase())){
                    //Error, LABEL is duplicated
                    System.out.println(LABEL + " is duplicated");
                    errorCheckArray[errorCheckIndex] = "        ***"+ LABEL + " is duplicated";
                    return;
                }
                mapLabel.put(LABEL, size);
                return;
            }
            else
            {
                for(int i = 0; i < size.length(); i++){
                    if(!(size.charAt(i) >= '0' && size.charAt(i) <= '9')){
                        //Error, size is not given in decimal format
                        ErrorLines.put(Linenumber,"      *** Error,size is not given in decimal format!");
                        return;
                    }
                }
                mapLabel.put(LABEL, 3 * Integer.parseInt(size) + "V") ; //V->size to be reserved in the memory, not a value assigned to that Label
                startAddress += 3 * Integer.parseInt(size);
            }
        }
        else if(opCode.toUpperCase().equals("BYTE")){
            if(isFirstTime)
            {
                if(mapLabel.containsKey(LABEL.toUpperCase())){
                    //Error, LABEL is duplicated
                    System.out.println(LABEL + " is duplicated");
                    ErrorLines.put(Linenumber,LABEL +"is duplicated");
                    errorCheckArray[errorCheckIndex] = "        *** "+ LABEL + " is duplicated";
                    return;
                }
                mapLabel.put(LABEL, size);
                return;
            }
            else
            {
                if(size.charAt(0) == 'C' || size.charAt(0) == 'c'){
                    if(!(size.charAt(1) == '\'' && size.charAt(size.length() - 1) == '\'')){
                        //Error, not given in correct format, single quotes not placed correctly
                        ErrorLines.put(Linenumber,"Error, not given in correct format, single quotes not placed correctly!");
                        System.out.println("Error, not given in correct format, single quotes not placed correctly!");
                        errorCheckArray[errorCheckIndex] = "      *** Error, not given in correct format, single quotes not placed correctly!";
                        return;
                    }
                    String insideQuotes = "";
                    for(int i = 2; i < size.length() - 1; i++){
                        insideQuotes = insideQuotes + size.charAt(i);
                    }

                    startAddress += insideQuotes.length();
                    mapLabel.put(LABEL, insideQuotes + "C");
                }
                else if(LABEL.charAt(0) == 'X' || LABEL.charAt(0) == 'x'){
                    if(!(startLabel.charAt(0) == '\'' && startLabel.charAt(startLabel.length() - 1) == '\'')){
                        //Error, not given in correct format, single quotes not placed correctly
                        ErrorLines.put(Linenumber,"Error, not given in correct format, single quotes not placed correctly!");
                        errorCheckArray[errorCheckIndex] = "      *** Error, not given in correct format, single quotes not placed correctly!";
                    }
                    String insideQuotes = "";
                    for(int i = 2; i < startLabel.length() - 1; i++){
                        insideQuotes = insideQuotes + startLabel.charAt(i);
                    }
                    for(int k = 0; k <  insideQuotes.length(); k++)
                    {
                        char aux = insideQuotes.charAt(k);
                        if(!(aux >= '0' && aux <= '9')){
                            if(!((aux >= 'a' && aux <= 'f') || (aux >= 'A' && aux <= 'F')))
                            {
                                System.out.println("ERROR, not in HEX Format range");
                                ErrorLines.put(Linenumber,"Error,not in HEX Formate range!");
                                errorCheckArray[errorCheckIndex] = "      *** Error,not in HEX Formate range!";
                                return;
                            }
                        }
                    }
                    startAddress += (insideQuotes.length() + 1) / 2;
                    mapLabel.put(LABEL, insideQuotes + "H");
                }
                else
                {
                    ErrorLines.put(Linenumber,"Error, not given in correct format, single quotes not placed correctly!");
                    errorCheckArray[errorCheckIndex] = "      *** Error, not given in correct format, single quotes not placed correctly!";
                }
            }
        }
        else if(opCode.toUpperCase().equals("WORD")){
            if(isFirstTime)
            {
                if(mapLabel.containsKey(LABEL.toUpperCase())){
                    //Error, LABEL is duplicated
                    System.out.println(LABEL + " is duplicated");
                    ErrorLines.put(Linenumber,LABEL+"is duplicated!");
                    errorCheckArray[errorCheckIndex] = "      *** "+LABEL+" is duplicated";
                    return;
                }
                mapLabel.put(LABEL, size);
                return;
            }
            else
            {
                if(size.charAt(0) == 'C' || size.charAt(0) == 'c'){
                    if(!(size.charAt(1) == '\'' && size.charAt(size.length() - 1) == '\'')){
                        //Error, not given in correct format, single quotes not placed correctly
                        errorCheckArray[errorCheckIndex] = "      *** (Check)Error, not given in correct format, single quotes not placed correctly!";
                        return;
                    }
                    String insideQuotes = "";
                    for(int i = 2; i < size.length() - 1; i++){
                        insideQuotes = insideQuotes + size.charAt(i);
                    }

                    startAddress += 3 * insideQuotes.length();
                    mapLabel.put(LABEL, insideQuotes + "C");
                }
                else if(LABEL.charAt(0) == 'X' || LABEL.charAt(0) == 'x'){
                    if(!(startLabel.charAt(0) == '\'' && startLabel.charAt(startLabel.length() - 1) == '\'')){
                        //Error, not given in correct format, single quotes not placed correctly
                        errorCheckArray[errorCheckIndex] = "      *** (Check)Error, not given in correct format, single quotes not placed correctly!";
                    }
                    String insideQuotes = "";
                    for(int i = 2; i < startLabel.length() - 1; i++){
                        insideQuotes = insideQuotes + startLabel.charAt(i);
                    }
                    for(int k = 0; k <  insideQuotes.length(); k++)
                    {
                        char aux = insideQuotes.charAt(k);
                        if(!(aux >= '0' && aux <= '9')){
                            if(!((aux >= 'a' && aux <= 'f') || (aux >= 'A' && aux <= 'F')))
                            {
                                // System.out.println("ERROR, not in HEXA Format range");
                                errorCheckArray[errorCheckIndex] = "      *** (Check)ERROR, not in HEXA Format range";
                                return;
                            }
                        }
                    }
                    startAddress += 3 * ((insideQuotes.length() + 1) / 2);
                    mapLabel.put(LABEL, insideQuotes + "H");
                }
            }
        }
        else if(opCode.toUpperCase().equals("ORG")) {

            for(int k = 1; k <  size.length(); k++)
            {
                char aux = size.charAt(k);
                if(!(aux >= '0' && aux <= '9')){
                    if(!((aux >= 'a' && aux <= 'f') || (aux >= 'A' && aux <= 'F')))
                    {
                        System.out.println("ERROR, not in HEX Format range");
                        ErrorLines.put(Linenumber,"Error, not in HEX Format range!");
                        errorCheckArray[errorCheckIndex] = "      *** ERROR, not in HEXA Format range";
                        return;
                    }
                }
            }

            String newAddress = "";
            for(int k = 1; k <  size.length(); k++)
            {
                newAddress +=size.charAt(1);
            }
            startAddress = HexToDEcimalConvert(newAddress);

        }
        else if(opCode.toUpperCase().equals("EQU"))
        {
            mapLabel.put(LABEL,size);
        }
        /*System.out.println("Label is: " + LABEL + " Opcode is: " + opCode + " Size is: " + size);
        System.out.println("Address in Decimal: " + startAddress);
        System.out.println("Address in Hexa: " + DecimalToHexa(startAddress));*/
    }

    protected int HexToDEcimalConvert(String HexString)
    {
        System.out.println(HexString + " YYy");
        if(HexString == null)
            return 0;
        return Integer.parseInt(HexString, 16);
    }

    protected String DecimalToHexa(int decimal){
        int rem = 0;
        String hex = "";
        char hexchars[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(decimal > 0)
        {
            rem = decimal % 16;
            hex = hexchars[rem] + hex;
            decimal = decimal / 16;
        }
        return hex;
    }

    private void generatingOpcode()
    {
        mapOpcode.put("RMO", 2);
        mapOpcode.put("LDA", 1);
        mapOpcode.put("LDS", 1);
        mapOpcode.put("LDT", 1);
        mapOpcode.put("STA", 1);
        mapOpcode.put("STS", 1);
        mapOpcode.put("STT", 1);
        mapOpcode.put("LDCH", 1);
        mapOpcode.put("STCH", 1);
        mapOpcode.put("ADD", 1);
        mapOpcode.put("SUB", 1);
        mapOpcode.put("ADDR", 2);
        mapOpcode.put("SUBR", 2);
        mapOpcode.put("COMP", 1);
        mapOpcode.put("COMPR", 2);
        mapOpcode.put("J", 1);
        mapOpcode.put("JEQ", 1);
        mapOpcode.put("JLT", 1);
        mapOpcode.put("JGT", 1);
        mapOpcode.put("TIX", 1);
        mapOpcode.put("TIXR", 1);

        mapOpcode4.put("+LDA", 1);
        mapOpcode4.put("+LDS", 1);
        mapOpcode4.put("+LDT", 1);
        mapOpcode4.put("+ST", 1);
        mapOpcode4.put("+LDCH", 1);
        mapOpcode4.put("+STCH", 1);
        mapOpcode4.put("+ADD", 1);
        mapOpcode4.put("+SUB", 1);
        mapOpcode4.put("+ADDR", 2);
        mapOpcode4.put("+COMP", 1);
        mapOpcode4.put("+J", 1);
        mapOpcode4.put("+JEQ", 1);
        mapOpcode4.put("+JLT", 1);
        mapOpcode4.put("+JGT", 1);
        mapOpcode4.put("+TIX", 1);

        mapDirective.put("BYTE", 1);
        mapDirective.put("RESW", 1);
        mapDirective.put("RESB", 1);
        mapDirective.put("WORD", 1);
        mapDirective.put("BASE", 1);
        mapDirective.put("ORG", 1);
        mapDirective.put("EQU", 1);

        mapRegisters.put("A", "0");
        mapRegisters.put("X", "0");
        mapRegisters.put("S", "0");
        mapRegisters.put("T", "0");

    }
}
