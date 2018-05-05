package editorconfig_test1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Test1 {
    
    /**
     * Программа анализирует отступы исходников с java-кодом.
     * В кач-ве аргумент в командной строке следует указать путь к файлу,
     * который следует проанализировать.
     * 
     * @param args - аргументы командной строки
     */
    public static void main(String[] args) {
        // список строк, в которые считается файл
        ArrayList<String> lines = new ArrayList();
        // переменная, показывающая, прошло ли чтение успешно
        boolean isReadedOK = false;
        
        /* Считываение файла */
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            isReadedOK = true;
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Error. Filename argument is necessary.");
        } catch (FileNotFoundException ex) {
            System.out.println("Error. File not found.");
        } catch (IOException ex) {
            System.out.println("IO exception:\n"+ex.toString());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                System.out.println("Closing file error:\n"+ex.toString());
            }
        }
        if (!isReadedOK) return;
        
        /* Выполнение анализа кода */
        Result result = TextAnalyser.analysis(lines);
        
        /* Вывод результата анализа */
        if (result.containsSpaces && result.containsTabs) {
            System.out.println(String.format(
                    "Indents contain both spaces and tabs.\n"+
                    "Most likely there are:\n"+
                    "  %d spaces per 1 tab\n"+
                    "  %d spaces per 1 indent\n"+
                    "  %d tabs per 1 indent\n", 
                    Math.round(result.spacesPerTab), 
                    Math.round(result.spacesPerIndent),
                    Math.round(result.tabsPerIndent)
            ));
        } else if (result.containsSpaces) {
            System.out.println(String.format(
                    "Indents contain only spaces.\n"+
                    "Most likely there are:\n"+
                    "  %d spaces per 1 indent\n", 
                    Math.round(result.spacesPerIndent)
            ));
        } else if (result.containsTabs) {
            System.out.println(String.format(
                    "Indents contain only tabs.\n"+
                    "Most likely there are:\n"+
                    "  %d tabs per 1 indent\n", 
                    Math.round(result.tabsPerIndent)
            ));
        } else {
            System.out.println("Code has no any indents or code format is too corrupted or unsupported by this program.");
        }
    }
}
