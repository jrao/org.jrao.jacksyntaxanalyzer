package org.jrao.jacksyntaxanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
	
	public static boolean isNullOrEmpty(String string) {
		if (string == null) {
			return true;
		}
		if (string.equals("")) {
			return true;
		}
		return false;
	}
	
	/*
	 * TODO: fix this method so that comments can be safely used inside of string literals
	 */
	public static String removeCommentsAndWhitespace(String string) {
		// regex from http://blog.ostermiller.org/find-comment
		String result = string.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
		result = result.trim();
		
		return result;
	}
	
	/*
	 * Opens the input file and gets ready to tokenize it
	 */
	public JackTokenizer(File inputFile) throws IOException {
		_scanner = new Scanner(inputFile);
		StringBuffer sb = new StringBuffer();
		while (_scanner.hasNextLine()) {
			sb.append(_scanner.nextLine() + "\n");
		}
		_input = sb.toString();
		_input = removeCommentsAndWhitespace(_input);
		
		_currentTokenIndex = -1;
		_tokenType = TokenType.UNKNOWN;
		
		populateTokenList();

		String inputFileParentPathString = inputFile.getParentFile().getAbsolutePath();
		String outputFileName = inputFile.getName();
		if (outputFileName.contains(".")) {
			outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf('.'));
		}
		outputFileName = outputFileName.concat(".vm");
		String outputPathString = inputFileParentPathString + "/" + outputFileName;
        File outputFile = new File(outputPathString);
		_bw = new BufferedWriter(new FileWriter(outputFile));
	}
	
	public void close() throws IOException {
		if (_bw != null) {
			_bw.flush();
			_bw.close();
		}
	}
	
	/*
	 * Are there more tokens in the input?
	 */
	public boolean hasMoreTokens() {
		if (_currentTokenIndex < _tokenList.size() - 1) {
			return true;
		}
		return false;
	}
	
	/*
	 * Gets the next token from the input and makes it the current token.
	 * Should only be called if hasMoreTokens() is true.
	 * Initially, there is no current token.
	 */
	public void advance() throws IOException {
		if (!hasMoreTokens()) {
			System.err.println("Error: Must not invoke advance() when there are no more tokens!");
			return;
		}
		
		_currentTokenIndex++;
		_currentToken = _tokenList.get(_currentTokenIndex);
		
		if (
			_currentToken.equals("class") || _currentToken.equals("constructor") || _currentToken.equals("function") ||
			_currentToken.equals("method") || _currentToken.equals("field") || _currentToken.equals("static") ||
			_currentToken.equals("var") || _currentToken.equals("int") || _currentToken.equals("char") ||
			_currentToken.equals("boolean") || _currentToken.equals("void") || _currentToken.equals("true") ||
			_currentToken.equals("false") || _currentToken.equals("null") || _currentToken.equals("this") ||
			_currentToken.equals("let") || _currentToken.equals("do") || _currentToken.equals("if") ||
			_currentToken.equals("else") || _currentToken.equals("while") || _currentToken.equals("return")
		) {
			_tokenType = TokenType.KEYWORD;
		}
		else if (
			_currentToken.equals("{") || _currentToken.equals("}") || _currentToken.equals("(") ||
			_currentToken.equals(")") || _currentToken.equals("[") || _currentToken.equals("]") ||
			_currentToken.equals(".") || _currentToken.equals(",") || _currentToken.equals(";") ||
			_currentToken.equals("+") || _currentToken.equals("-") || _currentToken.equals("*") ||
			_currentToken.equals("/") || _currentToken.equals("&") || _currentToken.equals("|") ||
			_currentToken.equals("<") || _currentToken.equals(">") || _currentToken.equals("=") ||
			_currentToken.equals("~")
		) {
			_tokenType = TokenType.SYMBOL;
		}
		else if (_currentToken.matches("[0-9]+")) {
			_tokenType = TokenType.INT_CONST;
		}
		else if (_currentToken.matches("\".*\"")) {
			_tokenType = TokenType.STRING_CONST;
		}
		else if (_currentToken.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
			_tokenType = TokenType.IDENTIFIER;
		}
		else {
			_tokenType = TokenType.UNKNOWN;
		}
	}
	
	/*
	 * Gets the previous token from the input and makes it the current token.
	 * Should only be called if _currentToken index is greater than 0.
	 */
	public void retreat() throws IOException {
		if (_currentTokenIndex < 1) {
			System.err.println("Error: Must not invoke when current token is less than 1!");
			return;
		}
		
		_currentTokenIndex--;
		_currentToken = _tokenList.get(_currentTokenIndex);
		
		if (
			_currentToken.equals("class") || _currentToken.equals("constructor") || _currentToken.equals("function") ||
			_currentToken.equals("method") || _currentToken.equals("field") || _currentToken.equals("static") ||
			_currentToken.equals("var") || _currentToken.equals("int") || _currentToken.equals("char") ||
			_currentToken.equals("boolean") || _currentToken.equals("void") || _currentToken.equals("true") ||
			_currentToken.equals("false") || _currentToken.equals("null") || _currentToken.equals("this") ||
			_currentToken.equals("let") || _currentToken.equals("do") || _currentToken.equals("if") ||
			_currentToken.equals("else") || _currentToken.equals("while") || _currentToken.equals("return")
		) {
			_tokenType = TokenType.KEYWORD;
		}
		else if (
			_currentToken.equals("{") || _currentToken.equals("}") || _currentToken.equals("(") ||
			_currentToken.equals(")") || _currentToken.equals("[") || _currentToken.equals("]") ||
			_currentToken.equals(".") || _currentToken.equals(",") || _currentToken.equals(";") ||
			_currentToken.equals("+") || _currentToken.equals("-") || _currentToken.equals("*") ||
			_currentToken.equals("/") || _currentToken.equals("&") || _currentToken.equals("|") ||
			_currentToken.equals("<") || _currentToken.equals(">") || _currentToken.equals("=") ||
			_currentToken.equals("~")
		) {
			_tokenType = TokenType.SYMBOL;
		}
		else if (_currentToken.matches("[0-9]+")) {
			_tokenType = TokenType.INT_CONST;
		}
		else if (_currentToken.matches("\".*\"")) {
			_tokenType = TokenType.STRING_CONST;
		}
		else if (_currentToken.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
			_tokenType = TokenType.IDENTIFIER;
		}
		else {
			_tokenType = TokenType.UNKNOWN;
		}
	}
	
	/*
	 * Return the type of the current token
	 */
	public TokenType tokenType() {
		return _tokenType;
	}
	
	/*
	 * Returns the keyword which is the current token. Should be called only
	 * when tokenType() is KEYWORD.
	 */
	public KeyWord keyWord() {
		String token = _currentToken.toLowerCase();
		switch (token) {
		case "class":
			return KeyWord.CLASS;
		case "method":
			return KeyWord.METHOD;
		case "function":
			return KeyWord.FUNCTION;
		case "constructor":
			return KeyWord.CONSTRUCTOR;
		case "int":
			return KeyWord.INT;
		case "boolean":
			return KeyWord.BOOLEAN;
		case "char":
			return KeyWord.CHAR;
		case "void":
			return KeyWord.VOID;
		case "var":
			return KeyWord.VAR;
		case "static":
			return KeyWord.STATIC;
		case "field":
			return KeyWord.FIELD;
		case "let":
			return KeyWord.LET;
		case "do":
			return KeyWord.DO;
		case "if":
			return KeyWord.IF;
		case "else":
			return KeyWord.ELSE;
		case "while":
			return KeyWord.WHILE;
		case "return":
			return KeyWord.RETURN;
		case "true":
			return KeyWord.TRUE;
		case "false":
			return KeyWord.FALSE;
		case "null":
			return KeyWord.NULL;
		case "this":
			return KeyWord.THIS;
		default:
			return KeyWord.UNKNOWN;
		}
	}

	/*
	 * Returns the symbol which is the current token. Should be called only
	 * when tokenType() is SYMBOL.
	 */
	public char symbol() {
		return _currentToken.charAt(0);
	}

	/*
	 * Returns the identifier which is the current token. Should be called only
	 * when tokenType() is IDENTIFIER.
	 */
	public String identifier() {
		return _currentToken;
	}

	/*
	 * Returns the integer value of the current token. Should be called only
	 * when tokenType() is INT_CONST.
	 */
	public int intVal() {
		return Integer.parseInt(_currentToken);
	}

	/*
	 * Returns the string value of the current token, without the double quotes.
	 * Should be called only when tokenType() is STRING_CONST.
	 */
	public String stringVal() {
		return _currentToken.replaceAll("\"", "");
	}
	
	public void writeXML() throws IOException {
		writeOpeningTokensTag();
		
		/*
		for (String token : _tokenList) {
			_bw.write(token + "\n");
		}
		*/
		while (hasMoreTokens()) {
			advance();
			switch (_tokenType) {
			case KEYWORD:
				_bw.write("<keyword> " + _currentToken + " </keyword>" + "\n");
				break;
			case SYMBOL:
				String escapedStr = _currentToken;
				if (escapedStr.equals("&")) {
					escapedStr = "&amp;";
				}
				else if (escapedStr.equals("<")) {
					escapedStr = "&lt;";
				}
				else if (escapedStr.equals(">")) {
					escapedStr = "&gt;";
				}
				_bw.write("<symbol> " + escapedStr + " </symbol>" + "\n");
				break;
			case INT_CONST:
				_bw.write("<integerConstant> " + _currentToken + " </integerConstant>" + "\n");
				break;
			case STRING_CONST:
				String strWithoutQuotes = _currentToken.replaceAll("\"", "");
				_bw.write("<stringConstant > " + strWithoutQuotes + " </stringConstant>" + "\n");
				break;
			case IDENTIFIER:
				_bw.write("<identifier> " + _currentToken + " </identifier>" + "\n");
				break;
			case UNKNOWN:
				_bw.write("<UNKNOWN> " + _currentToken + " </UNKNOWN>" + "\n");
				break;
			}
		}
		
		writeClosingTokensTag();
	}

	private void writeOpeningTokensTag() throws IOException {
		_bw.write("<tokens>\n");
	}

	private void writeClosingTokensTag() throws IOException {
		_bw.write("</tokens>\n");
	}
	
	private void populateTokenList() {
		_tokenList = new ArrayList<String>();
		
		//String keywordRegex = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
		String symbolRegex = "[\\{\\}\\(\\)\\[\\]\\.,;\\+\\-\\*/&\\|\\<\\>\\=~]";
		String intConstRegex = "[0-9]+";
		String strConstRegex = "\".*\"";
		String identifierRegex = "[a-zA-Z_][a-zA-Z0-9_]*";
		//String regex = keywordRegex + "|" + symbolRegex + "|" + intConstRegex + "|" + strConstRegex + "|" + identifierRegex;
		String regex = symbolRegex + "|" + intConstRegex + "|" + strConstRegex + "|" + identifierRegex;
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(_input);
		while (matcher.find()) {
			_tokenList.add(matcher.group());
		}
	}

	private Scanner _scanner;
	private BufferedWriter _bw;
	private List<String> _tokenList;
	private int _currentTokenIndex;
	private String _currentToken;
	private TokenType _tokenType;
	private String _input;

}
