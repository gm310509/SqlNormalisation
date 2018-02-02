/* Generated By:JavaCC: Do not edit this line. CommentAndLiteralParserConstants.java */
package com.teradata.edwdecoded.sql.normalise.commentandliteralgrammar;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CommentAndLiteralParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int OTHER_TEXT = 1;
  /** RegularExpression Id. */
  int STRING_LITERAL = 2;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 3;
  /** RegularExpression Id. */
  int SLASH = 6;
  /** RegularExpression Id. */
  int UNARY_MINUS = 7;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 8;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int ML_COMMENT_STATE = 1;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "<OTHER_TEXT>",
    "<STRING_LITERAL>",
    "\"/*\"",
    "\"*/\"",
    "<token of kind 5>",
    "<SLASH>",
    "<UNARY_MINUS>",
    "<SINGLE_LINE_COMMENT>",
  };

}