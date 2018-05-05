package editorconfig_test1;

import java.util.ArrayList;


/**
 * Класс, выполняющий анализ отступов java-кода
 */
public class TextAnalyser {
    
    private static ArrayList<String> lines;
    
    /// необходимые данные по предыдущим строкам
    private static Indent prevValidCodeLineIndent;
    private static boolean prevValidCodeLineOpenedCodeBlock;   
    //private static ArrayList<Indent> indents;
    private static ArrayList<Indent> openCodeBlockIndents;
    
    /// флаги состояния       
    private static boolean isOpenedCommentBlock;
    private static boolean gotoNextLine;
    private static boolean isNewLine;
    
    /// память по текущей строке
    private static Indent thisLineIndent;
    private static boolean thisLineContainsCode;
    private static boolean thisLineOpensCodeBlock;

    /// навигация
    private static int lineIndex;
    private static String line;
    private static int cur; // cursor
    
    /// результаты
    private static boolean existIndentsWithSpaces;
    private static boolean existIndentsWithTabs;
    private static int posSpacesPerTab;
    private static double posSpacesPerIndent;

    /**
     * Выполняет анализ отступов строк java-кода.
     * 
     * Если в результате указано, что пробелы и табуляции не содержатся
     * в отступах, значит, либо файл неправильно форматирован, 
     * либо отступов действительно нет.
     * 
     * @param lines - строки java-кода
     * @return - результат анализа
     */
    public static Result analysis(ArrayList<String> lines) {
        TextAnalyser.lines = lines;
        
        collectData();
        checkIfSpacesAndTabsExist();
        
        Result result = new Result();
        
        if (existIndentsWithSpaces && existIndentsWithTabs) {
            countSpacesPerTabAndFindIndent();
            findIndent();
            result.containsSpaces = true;
            result.containsTabs = true;
            result.spacesPerTab = posSpacesPerTab;
            result.spacesPerIndent = posSpacesPerIndent;
            result.tabsPerIndent = posSpacesPerIndent/posSpacesPerTab;
        } else if (existIndentsWithSpaces) {
            posSpacesPerTab=0;
            findIndent();
            //findOneTypeIndent();
            result.containsSpaces = true;
            result.containsTabs = false;
            result.spacesPerIndent = posSpacesPerIndent;
        } else if (existIndentsWithTabs) {
            posSpacesPerTab = 1;
            findIndent();
            //findOneTypeIndent();
            result.containsSpaces = false;
            result.containsTabs = true;
            result.tabsPerIndent = posSpacesPerIndent;
        } else {
            result.containsSpaces = false;
            result.containsTabs = false;
        }
            
        return result;        
    }
    
    /**
     * Сбор данных по отсупам.
     * 
     * Собирает в список те отступы, которые используются при открытии нового 
     * блока кода с помощью скобки '{', причем за отступ принимается разность
     * абсолютного отступа строки, следующей за строкой со скобкой '{',
     * и абсолютного оступа строки со скобкой '{'.
     * 
     */
    private static void collectData()  {
        
        prevValidCodeLineIndent = new Indent();
        prevValidCodeLineOpenedCodeBlock = false;
        
        isOpenedCommentBlock = false;   // многострочный комментарий был открыт
        gotoNextLine = false;            
        isNewLine = true;
        
        lineIndex = 0;
        line = lines.get(lineIndex);
        cur = 0; // cursor
        
        //indents = new ArrayList();
        openCodeBlockIndents = new ArrayList();
        
        
        while(true) {
            if (gotoNextLine) {   // обработка перехода к новой строке
                gotoNextLine = false;  // сброс флага
                
                if (thisLineContainsCode) {
                    //indents.add(thisLineIndent);
                    
                    if (prevValidCodeLineOpenedCodeBlock) {
                        prevValidCodeLineOpenedCodeBlock = false;
                        openCodeBlockIndents.add(thisLineIndent.sub(prevValidCodeLineIndent));
                    }
                    
                    if (thisLineOpensCodeBlock)
                        prevValidCodeLineOpenedCodeBlock = true;
                    
                    prevValidCodeLineIndent = thisLineIndent;
                }
                
                if (lineIndex+1 == lines.size()-1)
                    break;   // конец текста
                else {
                    lineIndex++;
                    line = lines.get(lineIndex);
                    isNewLine = true;
                }
            }
            
            
            if (isOpenedCommentBlock) {     // isOpenedCommentBlock == true
                if (cur==line.length()) {
                    gotoNextLine = true;
                    continue;
                }
                
                cur = line.indexOf("*/", cur);
                if (cur==-1) {                      // блок комментария не закрывается в этой строке
                    gotoNextLine = true;
                } else {                            // нашли, где закрывается блок комментария
                    cur+=2;
                    isOpenedCommentBlock=false;
                }
                
            } else {                        // isOpenedCommentBlock == false
                if (isNewLine) {                // isNewLine == true
                    isNewLine = false;              
                    
                    cur = 0;
                    thisLineContainsCode = false;
                    thisLineIndent = new Indent();
                    thisLineOpensCodeBlock = false;
                    
                    while (cur<line.length() && (line.charAt(cur)==' ' || line.charAt(cur)=='\t'))  {
                        if (line.charAt(cur)==' ')
                            thisLineIndent.spaces++;
                        else
                            thisLineIndent.tabs++;
                        cur++;
                    }
                    if (cur==line.length()) {
                        gotoNextLine=true;
                    }
                } else {                        // isNewLine == false
                    
                    if (cur==line.length()) {
                        gotoNextLine = true;
                        continue;
                    }
                    
                    if (line.charAt(cur)=='/') {
                        if (cur+1==line.length() || line.charAt(cur+1)=='/') {
                            gotoNextLine=true;
                        } else if (line.charAt(cur+1)=='*'){              
                            cur+=2;
                            isOpenedCommentBlock=true;
                        } else {
                            // значит это был символ кода
                            cur++;
                            thisLineContainsCode = true;
                        }
                        
                    } else {                        // line.charAt(cur)!='/'
                        // это код
                        thisLineContainsCode = true;
                        
                        // если символ '{' - крайний значимый символ в строке, то строку можно считать открывающей
                        if (line.charAt(cur) == '{')
                            thisLineOpensCodeBlock = true;
                        else if (thisLineOpensCodeBlock 
                                && line.charAt(cur)!=' ' 
                                && line.charAt(cur)!='\t') {
                            thisLineOpensCodeBlock = false;
                        }
                        
                        cur++;
                    }
                }
            }
        }
    }
        
    /**
     * Проверяет, встречаются ли пробелы и табуляции в найденной коллекции отступов
     */
    private static void checkIfSpacesAndTabsExist() {
        existIndentsWithSpaces = false;
        existIndentsWithTabs = false;
        
        for (Indent indent : openCodeBlockIndents) 
            if (indent.spaces>0) {
                existIndentsWithSpaces = true;
                break;
            }
        
        for (Indent indent : openCodeBlockIndents) 
            if (indent.tabs>0) {
                existIndentsWithTabs = true;
                break;
            }
    }
    
    
    /**
     *  Высчитывает число пробелов, приходящихся на одну табуляцию.
     * 
     *  Руботает только при наличии как пробелов, так и табуляций в отступах.
     *  Используется перебор всех возможных предполагаемых кол-в пробелов на одну табуляцию
     *      и выбирается одно них по принципу наибольшей вероятности.
     */
    private static void countSpacesPerTabAndFindIndent() {
        int maxSpacesPerTab = 16;       // предполагаемое максимально-возможножное число пробелов на одну табуляцию
        // i-му элементу соответсвует (i+1) пробелов на табуляцию
        double[] arrAvgSPI = new double[maxSpacesPerTab];  // average spaces per indent
        double[] arrMSD = new double[maxSpacesPerTab];  // mean square deviation
        
        double likeSpaceIndent; // весь отступ в "пробелах", включая приведенные табуляции
        
        for (int spt=1; spt<=maxSpacesPerTab; spt++) {
            for (Indent indent : openCodeBlockIndents) {
                likeSpaceIndent = indent.spaces + indent.tabs*spt;
                arrAvgSPI[spt-1] += likeSpaceIndent / openCodeBlockIndents.size();
            }
            for (Indent indent : openCodeBlockIndents) {
                likeSpaceIndent = indent.spaces + indent.tabs*spt;
                arrMSD[spt-1] += (likeSpaceIndent - arrAvgSPI[spt-1])*(likeSpaceIndent - arrAvgSPI[spt-1]);
            }
            arrMSD[spt-1] = Math.sqrt(arrMSD[spt-1] / openCodeBlockIndents.size());
            
            //System.out.println("spt="+spt+" avg="+arrAvgSPI[spt-1]+" msd="+arrMSD[spt-1]);
        }
        
        
        int posSPT = 1;
        for (int spt=2; spt<=maxSpacesPerTab; spt++) {
            if (arrMSD[spt-1]<arrMSD[posSPT-1])
                posSPT = spt;
        }
        
        posSpacesPerTab = posSPT;
        posSpacesPerIndent = arrAvgSPI[posSPT-1];
    }
    
    /**
     *  Ищет кол-во пробелов, приходящихся на один отступ.
     * 
     *  При расчете переводит табуляции в пробелы в соответсвии с кол-вом
     *      прообелов в одной табуляции.
     *  Находит самое часто повторяющееся кол-во пробелов в оступе и принимает
     *      его за искомое.
     */
    private static void findIndent() {
        int spt = Math.round(posSpacesPerTab);
        int likeSpaceIndent;
        int maxLikeSpaceIndent = 0;
        int[] frequency;
        for (Indent indent : openCodeBlockIndents) {
            likeSpaceIndent = indent.spaces + indent.tabs*spt;
            if (likeSpaceIndent > maxLikeSpaceIndent)
                maxLikeSpaceIndent = likeSpaceIndent;
        }
        frequency = new int[maxLikeSpaceIndent+1];
        for (Indent indent : openCodeBlockIndents) {
            likeSpaceIndent = indent.spaces + indent.tabs*spt;
            if (likeSpaceIndent>0)          // защита от особых случаев
                frequency[likeSpaceIndent] += 1;   
        }
        
        likeSpaceIndent = 0;
        for (int i=1; i<=maxLikeSpaceIndent; i++)
            if (frequency[i] > frequency[likeSpaceIndent])
                likeSpaceIndent = i;
        
        posSpacesPerIndent = likeSpaceIndent;
        
    }
}


