package editorconfig_test1;


/**
 * Контейнер результата анализа
 * 
 * @author akropon
 */
public class Result {
    // Существуют ли пробелы в отступах
    public boolean containsSpaces;
    // Существуют ли табуляции в отступах
    public boolean containsTabs;
    // Пробелов на одну табуляцию
    public double spacesPerTab;
    // Пробелов на один отступа
    public double spacesPerIndent;
    // Табуляций на один отступ
    public double tabsPerIndent;
}
