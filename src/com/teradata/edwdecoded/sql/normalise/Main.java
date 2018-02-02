/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teradata.edwdecoded.sql.normalise;

import com.teradata.edwdecoded.sql.normalise.commentandliteralgrammar.CommentAndLiteralParser;
import com.teradata.edwdecoded.sql.normalise.commentandliteralgrammar.ParseException;
import com.teradata.edwdecoded.sql.normalise.commentandliteralgrammar.TokenMgrError;
import com.teradata.fnc.DbsInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author glennm
 */
public class Main {

    /** Exit status code returned if the application succeeds. */
    public static final int APP_SUCCESS = 0;
    /** Exit status code returned if the application succeeds but with warnings. */
    public static final int APP_WARNING = 1;
    /** Exit status code returned if the application fails. */
    public static final int APP_FAILURE = 2;
    /** Status that indicates that the GUI has started, so do not exit. */
    public static final int APP_GUI = -1;

    /**
     * Entry point when run as a standalone program.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Add any other initialisation here.
        Main app = new Main();
        int stat;
        try {
            stat = app.go(args);
        } catch (Throwable t) {
            t.printStackTrace();
            stat = APP_FAILURE;
        }
        if (stat != APP_GUI) {
            System.exit(stat);
        }
    }

    /**
     * Entry point when run as a UDF.
     * 
     * @param sqlText the SQL Text to be normalised.
     * @return the normalised SQL.
     */
    public static String normaliseSqlUDF (String sqlText) {
        try {
            Debug.debugEnabled = false;
        
            if (sqlText == null) {
                return null;
            }

            Main m = new Main();
            return m.normalise(sqlText);
        } catch (Exception e ) {
            Debug.println("Exception caught: " + e);
            String inp = sqlText;
            if (inp != null && inp.length() > 100) {
                inp = inp.substring(0, 100);
            }
            String msg = "SqlNormalisationUDF: Exception: " + e + ", inp: " + inp;
            DbsInfo.traceWrite(msg);
            return msg;
        }
    }

    /**
     * The main line when run as a standalone program.
     * <p>
     * Calls the normalise method once for each command line parameter.
     * It is assumed that the command line parameter is the name of a file.
     * </p>
     * @param args the command line arguments.
     * @return the exit status code.
     */
    public int go(String [] args) {
        if (args.length == 0) {
            usage();
            return APP_SUCCESS;
        }
        
        for (String arg : args) {
            if ("-d".equalsIgnoreCase(arg)) {
                Debug.debugEnabled = false;
            } else if ("+d".equalsIgnoreCase(arg)) {
                Debug.debugEnabled = true;
            } else if ("-ui".equalsIgnoreCase(arg)) {
                MainFrame m = new MainFrame();
                m.setVisible(true);
                return APP_GUI;
            } else {
                normalise(new File(arg));
            }
        }
        
        return APP_SUCCESS;
    }
    
    
    /**
     * Output usage information.
     */
    private void usage() {
        Debug.println("Usage:");
        Debug.println("    normalise [-d|+d] file1 [[-d|+d] file2 ...]");
        Debug.println("    normalise -ui");
        Debug.println();
        Debug.println("Where:");
        Debug.println("  file1 is the name of a file containing SQL to be normalised.");
    }
    
    
    /**
     * Normalise the contents of the supplied file.
     * @see Main#normalise(java.lang.String) 
     * 
     * @param fileName the name of the file containing the text to be normalised.
     */
    public void normalise(File fileName) {
        BufferedReader br = null;
        try {
            String inLine;
            StringBuilder sb = new StringBuilder();
            
            br = new BufferedReader(new FileReader(fileName));
            while ((inLine = br.readLine()) != null) {
                sb.append(inLine);
                sb.append('\n');
            }
            String normalisedSql = normalise(sb.toString());
            System.out.println("Result from " + fileName.getName()+ ":\n" + normalisedSql);
            System.out.println();

        } catch (FileNotFoundException e) {
            Debug.println("Warning: " + fileName + " could not be openned:");
            Debug.println("         " + e.getMessage());
            Debug.println();
        } catch (IOException e) {
            Debug.println("Warning: error reading: " + fileName);
            Debug.println("         " + e.getMessage());
            Debug.println();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Debug.println("Warning: error closing: " + fileName);
                Debug.println("         " + e.getMessage());
                Debug.println();
            }
        }
    }
    
    
    /**
     * numeric regular expression...
     *      (\\W)               Any non word character.
     *      (
     *        [-+]?             Optional + or -
     *          (\\d+[\\.\\d]*  digits followed by optional decimal and more digits
     *          |               or
     *          \\.\\d+         a decimal point followed by one or more digits.
     *      )                   end on numeric definition.
     *      (E[+-]?\\d+)?)      exponent, optional +/- followed by at least one digit.
     */
    private Pattern numeric = Pattern.compile("(\\W)([-+]?(\\d+[\\.\\d]*|\\.\\d+)(E[+-]?\\d+)?)", Pattern.CASE_INSENSITIVE);
    //private Pattern hexadecimal = Pattern.compile("(\\W)'[0-9a-f]+'\\s*X(I[1248]?)?", Pattern.CASE_INSENSITIVE);
    /**
     * Hexadecimal regular expression...
     *      (\\W)               Anything that is not a word
     *      \\*STR_LITERAL\\*   The text *STR_LITERAL* - because the parser will have converted the hex string (e.g. '10ff') to *STR_LITERAL*
     *      X                   The letter X (NB: there can be no whitespace between the string literal and the letter X.
     *      (I                  A letter "I" (letter eye)
     *          [1248]?         An optional word size
     *      )?                  The letter I and word size is optional
     * 
     */
    private Pattern hexadecimal = Pattern.compile("(\\W)\\*STR_LITERAL\\*X(I[1248]?)?", Pattern.CASE_INSENSITIVE);
    
    /**
     * Normalise the query text provided in the parameter.
     * <p>
     * Normalisation includes:
     * <ul>
     *   <li>Removal of comments</li>
     *   <li>collapsing whitespace and newlines to a single space</li>
     *   <li>removing leading and trailing whitespace and newlines</li>
     *   <li>replacing literal values with a token</li>
     * </ul>
     * </p>
     * @param sqlText the sqlText to be normalised.
     * @return the normalised query text.
     */
    public String normalise(String sqlText) {
        Debug.println("Normalising:\n\"" + sqlText + "\"");
        
        if (sqlText == null) {
            return null;
        }

        String normalisedSql;
        
        CommentAndLiteralParser parser = new CommentAndLiteralParser(new StringReader(sqlText));
        StringBuilder sb = new StringBuilder();
        try {
            String result = parser.normaliseSql(sb);
            normalisedSql = sb.toString();

            /* Trim down any whitespace */
            normalisedSql = normalisedSql.replaceAll("\\s+", " ");
            normalisedSql = normalisedSql.replaceFirst("^\\s+", "");
            normalisedSql = normalisedSql.replaceFirst("\\s+$", "");
            
            /* Remove any standalone numbers */
            normalisedSql = replaceAll(numeric, normalisedSql, "*N*");
            /* Remove and standaline hexadecimal literals. */
            normalisedSql = replaceAll(hexadecimal, normalisedSql, "*H*");
            
        } catch (ParseException e) {
            normalisedSql = parseError(e, sqlText);
        } catch (TokenMgrError e) {
            normalisedSql = parseError(e, sqlText);
        }

        return normalisedSql;
    }

    /**
     * Apply the supplied pattern to the sql Text and replace any matches with the
     * replacement.
     * <p>
     * The method assumes that there is a group that preceeds the match text.
     * Any text found in the group is included in the replacement.
     * </p>
     * @param pattern
     * @param sqlText
     * @param replacement
     * @return 
     */
    private String replaceAll(Pattern pattern, String sqlText, String replacement) {
        int cnt = 0;
        Matcher m = pattern.matcher(sqlText);
        while (m.find()) {
            cnt++;
            String grp1 = m.group(1);
            Debug.println("Match " + cnt + ": " + m.group());
            if (grp1 != null) {
                sqlText = m.replaceFirst(grp1 + replacement);
            } else {
                sqlText = m.replaceFirst(replacement);
            }
            m = pattern.matcher(sqlText);
        }
        return sqlText;
    }


    
    /**
     * Format a parse error for output.
     * @param t the exception
     * @param originalSql the sql being parsed that generated the error.
     * @return Returns the original Sql and a comment containing the error text.
     * 
     */
    public String parseError(Throwable t, String originalSql) {
        Debug.println("Error processing sql Text. Using input SqlText.");
        String msg = t.getMessage();
        Debug.println(msg);

        msg = msg.replaceAll("/\\*", "|*");
        msg = msg.replaceAll("\\*/", "*|");
        return "/* Parse Error " + msg + ". Input SQL: */ " + originalSql;

    }
}
