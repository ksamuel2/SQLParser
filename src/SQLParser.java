import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class SQLParser {
    private static final Exception Exception = null;
    /*
    * Executable main method that takes the user into a console scanner where they input commands that
    * perform specific actions on a database style text file that separates cells by a : delimiter
    * @args - This program takes one argument and that is the directory path to the file you want to operate on
    * For example if you wanted to access the file Users/Admin/Desktop/sample.txt the argument would be
    * Users/Admin/Desktop/ and when you specify the file you want to modify you should omit the extension, for example
    * SELECT * FROM sample
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        directory = args[0];
        int done = 0;
        while(done == 0) {
            String command = scanner.nextLine();
            if(command.substring(0, 1).equalsIgnoreCase("s")) {
                executeSelect(command);
            }
            else if(command.substring(0, 1).equalsIgnoreCase("i")) {
                executeInsert(command);
            }
            else if(command.substring(0, 1).equalsIgnoreCase("d")) {
                if(command.toLowerCase().contains("where"))
                    executeDeleteWhere(command);
                else executeDelete(command);
            }
            else if(command.substring(0, 1).equalsIgnoreCase("u")) {
                executeUpdate(command);
            }
            else if(command.substring(0, 1).equalsIgnoreCase("q")) {
                done = 1;
            }
            else {
                System.out.println("Invalid command");
            }
        }
        scanner.close();
        System.out.println("Program terminated successfully");
    }
    /*
     * @param command - The command that was read in from the scanner to be parsed into arguments
     * This is a helper function that parses the command into lists so that they are correctly
     * formatted for the select function to run, also executes the select function from within
     */
    private static void executeSelect(String command) {
        try {
            if(!command.substring(0, 7).equalsIgnoreCase("SELECT ")) {
                System.err.print("Invalid syntax for select statement");
                return;
            }
            command = command.substring(6).trim();
            String[] commandArr = command.split("(?i)FROM");
            String values = commandArr[0].trim();
            values = values.replaceAll("[()]", "");
            StringTokenizer st = new StringTokenizer(values, ",");
            ArrayList<String> columnVal = new ArrayList<String>();
            boolean selectAll = false;
            if(values.equals("*")) selectAll = true;
            while(st.hasMoreTokens())
                columnVal.add(st.nextToken().trim());
            String fileName = commandArr[1].trim();
            select(columnVal, fileName, selectAll);
        } catch (Exception e) {
            System.err.println("Invalid syntax for select statement exception generated while parsing");
        }
    }
    /*
     * @param columnVal - The names of the keys of the columns you would like to select to be printed
     * @param fileName - The name of the file path of the text file to select from
     * @param selectAll - A trigger value to select all the columns in the table
     * This is a helper method that takes in the parsed select command parameters and then prints
     * the selected columns to the console
     */
    private static void select(ArrayList<String> columnVal, String fileName, boolean selectAll) {
        fileName = directory + fileName + ".txt";
        fileArray = textFileToArray(fileName);
        ArrayList<Integer> keys = new ArrayList<Integer>();
        for(int i = 0; i < fileArray.get(0).size(); i++) {
            for(String keyVal : columnVal) {
                if(selectAll) keys.add((Integer) i);
                else if(keyVal.equalsIgnoreCase(fileArray.get(0).get(i))) keys.add((Integer) i);
            }
        }
        for(ArrayList<String> lineArray : fileArray) {
            int count = 0;
            String printVal = "";
            for(String word : lineArray) {
                boolean existsInKeys = false;
                for(Integer key : keys) {
                    if(key.equals((Integer) count)) {
                        existsInKeys = true;
                        break;
                    }
                }
                if(existsInKeys) printVal += word + ":";
                count++;
            }
            printVal = printVal.substring(0, printVal.length() - 1);
            System.out.println(printVal);
        }
    }
    /*
     * @param command - The command that was read in from the scanner to be parsed into arguments
     * This is a helper function that parses the command into lists so that they are correctly
     * formatted for the insert function to run, also executes the insert function from within
     */
    private static void executeInsert(String command) {
        try {
            if(!command.substring(0, 12).equalsIgnoreCase("INSERT INTO ")) {
                System.err.print("Invalid syntax for insert statement");
                return;
            }
            command = command.substring(12).trim();
            String[] commandArr = command.split("(?i)VALUES");
            commandArr[0] = commandArr[0].trim();
            String[] columnValueArray = commandArr[0].split("[(]");
            ArrayList<String> columnVal = new ArrayList<String>();
            String fileName;
            boolean allColumns = false;
            ArrayList<String> values = new ArrayList<String>();
            if(columnValueArray.length == 1) {
                fileName = columnValueArray[0].trim();
                allColumns = true;
            }
            else {
                fileName = columnValueArray[0].trim();
                columnValueArray[1] = columnValueArray[1].replaceAll("[()]", "").trim();
                String[] onlyColumnValues = columnValueArray[1].split(",");
                for(String columnValue : onlyColumnValues)
                    columnVal.add(columnValue.trim());
            }
            commandArr[1] = commandArr[1].replaceAll("[()]", "").trim();
            String[] onlyValues = commandArr[1].split(",");
            for(String value : onlyValues)
                values.add(value.trim());
            insert(fileName, columnVal, values, allColumns);
        } catch (Exception e) {
            System.err.println("Invalid syntax for insert statement exception generated while parsing");
        }
    }
    /*
     * @param columnVal - The names of the keys of the columns you would like to insert
     * @param fileName - The name of the file path of the text file to insert to
     * @param values - The raw values that are to be inserted
     * @param selectAll - A trigger value to insert to all columns
     * This is a helper method that takes in the parsed insert command parameters and then
     * appends them to the file
     */
    private static void insert(String fileName, ArrayList<String> columnVal, ArrayList<String> values, boolean allColumns) {
        fileName = directory + fileName + ".txt";
        fileArray = textFileToArray(fileName);
        if(allColumns) columnVal = fileArray.get(0);
        if(!checkValidity(columnVal, values)) {
            System.err.println("Invalid input for insert");
            return;
        }

        ArrayList<String> row = new ArrayList<String>();
        for(int i = 0; i < fileArray.get(0).size(); i++) row.add("null");
        for(int i = 0; i < fileArray.get(0).size(); i++) {
            for(int j = 0; j < columnVal.size(); j++) {
                if(fileArray.get(0).get(i).equalsIgnoreCase(columnVal.get(j)))
                    row.set(i, values.get(j));
            }
        }
        fileArray.add(row);
        writeArrayToFile(fileArray, fileName);
    }
    /*
     * @param columnVal - The names of the keys of the columns you would like to insert values to
     * @param values - The raw values that are to be inserted
     * This is a helper method that validates the operation by checking if lengths match and if the
     * values are of the correct type etc.
     */
    private static boolean checkValidity(ArrayList<String> columnVal, ArrayList<String> values) {
        if(columnVal.size() != values.size()) return false;
        return true;
    }
    /*
     * @param command - The command that was read in from the scanner to be parsed into arguments
     * This is a helper function that parses the command into lists so that they are correctly
     * formatted for the update function to run, also executes the update function from within
     * also makes a call to the executeWhere helper function
     */
    private static void executeUpdate(String command) {
        if(!command.substring(0, 7).equalsIgnoreCase("UPDATE ")) {
            System.err.println("Invalid syntax for update statement");
            return;
        }
        command = command.substring(6).trim();
        String[] parseFileName = command.split(" (?i)SET ");
        if(parseFileName.length == 1) {
            System.err.println("Invalid syntax for update statement");
            return;
        }
        String fileName = parseFileName[0].trim();
        fileName = directory + fileName + ".txt";
        fileArray = textFileToArray(fileName);
        command = parseFileName[1];
        String[] parseWhereClause = command.split(" (?i)WHERE");
        boolean selectAll = false;
        if(parseWhereClause.length ==  1) selectAll = true;
        String valueArrString = parseWhereClause[0].trim();
        String[] valueArr = valueArrString.split(",");
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> columnValues = new ArrayList<String>();
        for(String valAssign : valueArr) {
            String[] valAssignArr = valAssign.split("=");
            if(valAssignArr.length != 2) {
                System.err.println("Invalid syntax for update statement");
                return;
            }
            columnValues.add(valAssignArr[0].trim());
            values.add(valAssignArr[1].trim());
        }
        ArrayList<Integer> rows = new ArrayList<Integer>();
        if(!selectAll) {
            rows = executeWhere(parseWhereClause[1]);
        }
        update(fileName, columnValues, values, rows, selectAll);


    }
    /*
    * @param fileName - The name of the file that you would like to update
    * @param columnValues - Parsed values from the executeUpdate function, the columns you want to update
    * @param values - Parsed values from the executeUpdate function, the values you want to store into the columns
    * @param rows - Parsed values from the executeUpdate function, the rows that are to be updated
    * @param selectAll - A trigger that modifies every row in the array
    * This function updates values in the file that you specify and is to be used according to the proper SQL syntax
     */
    private static void update(String fileName, ArrayList<String> columnValues, ArrayList<String> values, ArrayList<Integer> rows, boolean selectAll) {
        for(int i = 0; i < columnValues.size(); i++) {
            String columnVal = columnValues.get(i);
            String value = values.get(i);
            int key = getColumnIndex(columnVal);
            if(!selectAll) {
                for (Integer rowIndex : rows) {
                    fileArray.get(rowIndex).set(key, value);
                }
            }
            if(selectAll) {
                for(int rowIndex = 0; rowIndex < fileArray.size(); rowIndex++) {
                    fileArray.get(rowIndex).set(key, value);
                }
            }
        }
        writeArrayToFile(fileArray, fileName);
    }
    /*
     * @param command - The command that was read in from the scanner to be parsed into arguments
     * This is a helper function that parses the command into lists so that they are correctly
     * formatted for the delete function to run, also executes the delete function from within
     */
    private static void executeDelete(String command) {
        try {
            if(!command.substring(0, 7).equalsIgnoreCase("DELETE ")) {
                System.err.println("Invalid syntax for select statement");
                return;
            }
            command = command.substring(6).trim();
            String[] commandArr = command.split("(?i)FROM");
            String values = commandArr[0].trim();
            values = values.replaceAll("[()]", "");
            StringTokenizer st = new StringTokenizer(values, ",");
            ArrayList<Integer> rows = new ArrayList<Integer>();
            boolean deleteAll = false;
            while(st.hasMoreTokens())
                rows.add(Integer.parseInt(st.nextToken().trim()));
            if(rows.size() == 0) deleteAll = true;
            String fileName = commandArr[1].trim();
            fileName = directory + fileName + ".txt";
            fileArray = textFileToArray(fileName);
            delete(fileName, rows, deleteAll);
        } catch (Exception e) {
            System.err.println("Invalid syntax for delete statement exception thrown while parsing");
        }
    }
    /*
     * @param command - The command that was read in from the scanner to be parsed into arguments
     * This is a helper function that parses the command into lists so that they are correctly
     * formatted for the delete function to run, also executes the delete function from within
     * This varies from executeDelete as it is used when a where clause is found to be present
     * in the command
     */
    private static void executeDeleteWhere(String command) {
        if(!command.substring(0, 12).equalsIgnoreCase("DELETE FROM ")) {
            System.err.println("Invalid syntax for delete statement");
            return;
        }
        command = command.substring(12).trim();
        String[] commandArr = command.split("(?i)WHERE");
        String fileName = commandArr[0].trim();
        if(commandArr.length == 1) {
            System.err.println("Invalid syntax for delete statement");
            return;
        }
        fileName = directory + fileName + ".txt";
        fileArray = textFileToArray(fileName);
        String whereCommand = commandArr[1].trim().replaceAll("[()]", "");
        ArrayList<Integer> keys = executeWhere(whereCommand);

        int index;
        while((index = keys.indexOf(0)) != -1) {
            keys.remove(index);
        }
        if(keys == null) return;
        delete(fileName, keys, false);
    }

    /*
     * @param fileName - The name of the file path of the text file to insert to
     * @param rows - The indices of the rows that are to be deleted
     * @param deleteAll - A trigger value to delete all columns
     * This is a helper method that takes in the parsed delete command parameters and then deletes
     * the according indices and writes the updated information to the file
     */
    private static void delete(String fileName, ArrayList<Integer> rows, boolean deleteAll) {
        ArrayList<ArrayList<String>> newFileArray = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < fileArray.size(); i++) {
            boolean found = false;
            for(int j = 0; j < rows.size(); j++) {
                if(((Integer) i).equals(rows.get(j)))
                    found = true;
            }
            if(!found) newFileArray.add(fileArray.get(i));
        }
        writeArrayToFile(newFileArray, fileName);
    }

    /*
     * @param where - This is a string representation of the clause following the occurance of where in the command
     * This is a helper function that parses the where clause string into information that can be evaluated by the where
     * function, it returns the keys of all the occurances where the conditions were found to be satisfied
     */
    private static ArrayList<Integer> executeWhere(String where) {
        where = where.trim().replaceAll("[()]", "");
        String[] expressions = where.split("(?i) AND |(?i) OR ");
        String[] expressionsWithLogic = where.split("((?<=(?i) AND )|(?=(?i) AND )|(?<=(?i) OR )|(?=(?i) OR ))");
        ArrayList<String> valueA = new ArrayList<String>();
        ArrayList<String> valueB = new ArrayList<String>();
        ArrayList<Integer> operator = new ArrayList<Integer>();
        ArrayList<Integer> logic = new ArrayList<Integer>();
        for(String logicVal : expressionsWithLogic) {
            if(logicVal.trim().equalsIgnoreCase("AND")) {
                logic.add(AND);
            }
            if(logicVal.trim().equalsIgnoreCase("OR")) {
                logic.add(OR);
            }
        }
        for(String exp : expressions) {
            exp = exp.trim();
            String[] expArray = exp.split("((?<=(?i) = )|(?=(?i) = )|(?<=(?i) != )|(?=(?i) != ))");
            if(expArray.length == 1) {
                System.err.println("Invalid syntax for where clause");
                return null;
            }
            valueA.add(expArray[0].trim());
            valueB.add(expArray[2].trim());
            if(expArray[1].trim().equals("=")) operator.add(EQ);
            else if(expArray[1].trim().equals("!=")) operator.add(NEQ);
            else {
                System.err.println("Invalid syntax for where clause");
                return null;
            }
        }
        ArrayList<Integer> keys = where(valueA, valueB, operator, logic);
        return keys;
    }
    /*
     * WHERE (CAT = DOG AND MOUSE = MICE OR FLIP != FLOP)
     * @param valueA - In the example above, this would consist of (CAT, MOUSE, FLIP)
     * @param valueB - In the example above, this would consist of (DOG, MICE, FLOP)
     * @param operator - An integer assignment for each of the operators =, !=, etc.
     * @param logic - An integer assignment for each of the logic operators AND or OR
     * This is a helper function that takes in the values parsed by executeWhere and then evaluates all the conditions stated and returns
     * a list of all the rows where the conditions were found to be true
     */
    private static ArrayList<Integer> where(ArrayList<String> valueA, ArrayList<String> valueB, ArrayList<Integer> operator, ArrayList<Integer> logic) {
        ArrayList<ArrayList<Integer>> keys = new ArrayList<ArrayList<Integer>>();
        for(int i = 0; i < valueA.size(); i++) {
            String columnVal = valueA.get(i);
            ArrayList<String> column = getColumn(columnVal);
            if(column == null) return null;
            String key = valueB.get(i);
            int rowIndex = 0;
            ArrayList<Integer> columnKeys = new ArrayList<Integer>();
            for(String word : column) {
                if(operator.get(i).equals(EQ)) {
                    if(word.equalsIgnoreCase(key)) {
                        columnKeys.add(rowIndex);
                    }
                }
                if(operator.get(i).equals(NEQ)) {
                    if(!word.equalsIgnoreCase(key)) {
                        columnKeys.add(rowIndex);
                    }
                }
				/*Add in all necessary boolean operators*/
                rowIndex++;
            }
            keys.add(columnKeys);
        }
        return evaluateBooleans(keys, logic);
    }
    /*
     * @param keys - A list of lists of integers where conditions were found to be true
     * @param logic - A corresponding list of logic operators between the lists
     * A helper function that after all the conditions are evaluated, simplifies the boolean expression and evaluates all the
     * logic operators
     */
    private static ArrayList<Integer> evaluateBooleans(ArrayList<ArrayList<Integer>> keys, ArrayList<Integer> logic) {
        while(logic.size() != 0) {
            for(int i = 0; i < logic.size(); i++) {
                if(logic.get(i).equals((Integer) AND)) {
                    ArrayList<Integer> newKeys = new ArrayList<Integer>();
                    ArrayList<Integer> setA = keys.get(i);
                    ArrayList<Integer> setB = keys.get(i + 1);
                    for(Integer j : setA) {
                        for(Integer k : setB) {
                            if(j.equals(k)) {
                                newKeys.add(j);
                                break;
                            }
                        }
                    }
                    logic.remove(i);
                    keys.remove(i);
                    keys.set(i, newKeys);
                }
            }
            for(int i = 0; i < logic.size(); i++) {
                if(logic.get(i).equals((Integer) OR)) {
                    ArrayList<Integer> setA = keys.get(i);
                    for(Integer j : setA) {
                        boolean found = false;
                        for(Integer k : keys.get(i + 1)) {
                            if(j.equals(k)) found = true;
                        }
                        if(!found)
                            keys.get(i + 1).add(j);
                    }
                    logic.remove(i);
                    keys.remove(i);
                }
            }
        }
        return keys.get(0);
    }
    /*
     * @param columnName - The name of the column you want to retrieve
     * A simple helper function that returns the column with the corresponding name
     */
    private static ArrayList<String> getColumn(String columnName) {
        int key = -1;
        for(int i = 0; i < fileArray.get(0).size(); i++) {
            if(columnName.equalsIgnoreCase(fileArray.get(0).get(i))) {
                key = i;
                break;
            }
        }
        if(key == -1) {
            System.err.println("Column value you specified was not found in file");
            return null;
        }
        ArrayList<String> column = new ArrayList<String>();
        for(ArrayList<String> row : fileArray)
            column.add(row.get(key));
        return column;
    }
    /*
     * @param columnNAme - The name of the column whose index you want to find
     * A helper function that returns the index of the column with the corresponding name
     */
    private static int getColumnIndex(String columnName) {
        int key = -1;
        for(int i = 0; i < fileArray.get(0).size(); i++) {
            if(columnName.equalsIgnoreCase(fileArray.get(0).get(i))) {
                key = i;
                break;
            }
        }
        return key;
    }
    /*
     * @param fileName - The file path of the text file to be operated on
     * This method is a helper method that converts the text file into an easy to
     * manipulate double ArrayList of strings.
     */
    private static ArrayList<ArrayList<String>> textFileToArray(String fileName) {
        ArrayList<ArrayList<String>> fileArrayValue = new ArrayList<ArrayList<String>>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;
            while((line = br.readLine()) != null) {
                if(line.trim().isEmpty()) continue;
                if(line.substring(0, 1).equals("#")) continue;
                fileArrayValue.add(new ArrayList<String>());
                StringTokenizer st = new StringTokenizer(line, ":");
                while(st.hasMoreTokens()) {
                    fileArrayValue.get(lineNumber).add(st.nextToken());
                }
                lineNumber++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.err.println("FileNotFoundException generated while attempting to read from text file");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("IOException generated while attempting to read from text file");
            e.printStackTrace();
            return null;
        }
        return fileArrayValue;
    }
    /*
     * @param fileArrayValue - A double ArrayList of strings containing all information to be written to file
     * @param fileName - The name of the file path you would like to write to
     * This method is a helper method that takes in a double string array and writes it out to the file
     * you want
     */
    private static void writeArrayToFile(ArrayList<ArrayList<String>> fileArrayValue, String fileName) {
        try {
            File output = new File(fileName);
            if(!output.exists()) output.createNewFile();
            PrintWriter pw = new PrintWriter(output);
            pw.print("");
            pw.close();
            FileOutputStream fos = new FileOutputStream(output);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for(ArrayList<String> lineArray : fileArrayValue) {
                String line = "";
                for(String word : lineArray) {
                    line += word + ":";
                }
                line = line.substring(0, line.length() - 1);
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.err.println("FileNotFoundException generated while writing information to file");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("IOException generated while writing information to file");
            e.printStackTrace();
        }
    }
    private static final int AND = 0;
    private static final int OR = 1;
    private static final int LT = 0;
    private static final int LTE = 1;
    private static final int EQ = 2;
    private static final int NEQ = 3;
    private static final int GT = 4;
    private static final int GTE = 5;
    private static String directory;
    private static ArrayList<ArrayList<String>> fileArray;
}
