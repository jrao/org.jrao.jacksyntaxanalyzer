package org.jrao.jacksyntaxanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
	
	public static String getEscapedSymbol(char symbol) {
		if (symbol == '&') {
			return "&amp;";
		}
		else if (symbol == '<') {
			return "&lt;";
		}
		else if (symbol == '>') {
			return "&gt;";
		}
		else {
			return String.valueOf(symbol);
		}
	}

	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		_bw = new BufferedWriter(new FileWriter(outputFile));
		_tokenizer = new JackTokenizer(inputFile);
	}
	
	public void close() throws IOException {
		if (_bw != null) {
			_bw.flush();
			_bw.close();
		}
	}
	
	public void compileClass() throws IOException {
		_bw.write("<class>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || !_tokenizer.keyWord().equals(KeyWord.CLASS)) {
			System.err.println("Error compiling class!");
			return;
		}
		_bw.write("<keyword> class </keyword>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling class!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier() + " </identifier>\n");
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() !='{') {
			System.err.println("Error compiling class!");
			return;
		}
		_bw.write("<symbol> " + '{' + " </symbol>\n");
		
		compileClassVarDec();
		compileSubroutine();
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() !='}') {
			System.err.println("Error compiling class!");
			return;
		}
		_bw.write("<symbol> " + '}' + " </symbol>\n");
		
		if (_tokenizer.hasMoreTokens()) {
			System.err.println("Error: There should be no more tokens!");
			return;
		}
		
		_bw.write("</class>\n");
	}
	
	public void compileClassVarDec() throws IOException {
		_tokenizer.advance();

		// Check for end of class
		if (_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() == '}') {
			_tokenizer.retreat();
			return;
		}
		
		// Check for end of variable declarations (i.e., check for beginning of subroutine declarations)
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD) && (_tokenizer.keyWord().equals(KeyWord.CONSTRUCTOR)
				|| _tokenizer.keyWord().equals(KeyWord.FUNCTION) || _tokenizer.keyWord().equals(KeyWord.METHOD))) {

			_tokenizer.retreat();
			return;
		}
		
		_bw.write("<classVarDec>\n");
		
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || !(_tokenizer.keyWord().equals(KeyWord.STATIC)
				|| _tokenizer.keyWord().equals(KeyWord.FIELD))) {

			System.err.println("Error compiling class variables declaration!");
			return;
		}
		if (_tokenizer.keyWord().equals(KeyWord.STATIC)) {
			_bw.write("<keyword> static </keyword>\n");
		}
		else {
			_bw.write("<keyword> field </keyword>\n");
		}
		
		// Handle type
		_tokenizer.advance();
		if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.IDENTIFIER))) {
			System.err.println("Error compiling class variables declaration!");
			return;
		}
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
			if (!(_tokenizer.keyWord() == KeyWord.INT || _tokenizer.keyWord() == KeyWord.CHAR
					|| _tokenizer.keyWord() == KeyWord.BOOLEAN)) {

				System.err.println("Error compiling class variables declaration!");
				return;
			}
			if (_tokenizer.keyWord().equals(KeyWord.INT)) {
				_bw.write("<keyword> int </keyword>\n");
			}
			else if (_tokenizer.keyWord().equals(KeyWord.CHAR)) {
				_bw.write("<keyword> char </keyword>\n");
			}
			else {
				_bw.write("<keyword> boolean </keyword>\n");
			}
		}
		else {
			_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		}

		// Handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling class variables declaration!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		// Handle ',' or ';'
		handleVariableDeclarationList();

		_bw.write("</classVarDec>\n");
		
		compileClassVarDec();
	}
	
	public void compileSubroutine() throws IOException {
		_tokenizer.advance();

		// Check for end of class
		if (_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() == '}') {
			_tokenizer.retreat();
			return;
		}
		
		if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) && (_tokenizer.keyWord().equals(KeyWord.CONSTRUCTOR)
				|| _tokenizer.keyWord().equals(KeyWord.FUNCTION) || _tokenizer.keyWord().equals(KeyWord.METHOD)))) {

			System.err.println("Error compiling subroutines!");
			return;
		}
		
		_bw.write("<subroutineDec>\n");
		
		_bw.write("<keyword> " + _tokenizer.keyWord().toString().toLowerCase()  + " </keyword>\n");
		
		// Handle return type
		_tokenizer.advance();
		if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.IDENTIFIER))) {
			System.err.println("Error compiling subroutines!");
			return;
		}
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
			if (!(_tokenizer.keyWord() == KeyWord.VOID || _tokenizer.keyWord() == KeyWord.INT
					|| _tokenizer.keyWord() == KeyWord.CHAR || _tokenizer.keyWord() == KeyWord.BOOLEAN)) {

				System.err.println("Error compiling class variables declaration!");
				return;
			}
			if (_tokenizer.keyWord().equals(KeyWord.VOID)) {
				_bw.write("<keyword> void </keyword>\n");
			}
			else if (_tokenizer.keyWord().equals(KeyWord.INT)) {
				_bw.write("<keyword> int </keyword>\n");
			}
			else if (_tokenizer.keyWord().equals(KeyWord.CHAR)) {
				_bw.write("<keyword> char </keyword>\n");
			}
			else {
				_bw.write("<keyword> boolean </keyword>\n");
			}
		}
		else {
			_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		}
		
		// Handle subroutine name
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling subroutines!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		// Handle parameter list
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '(') {
			System.err.println("Error compiling subroutines!");
			return;
		}
		_bw.write("<symbol> " + '('  + " </symbol>\n");
		
		compileParameterList();

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
			System.err.println("Error compiling subroutines!");
			return;
		}
		_bw.write("<symbol> " + ')'  + " </symbol>\n");
		
		compileSubroutineBody();
		
		_bw.write("</subroutineDec>\n");
		
		compileSubroutine();
	}
	
	private void compileSubroutineBody() throws IOException {
		_bw.write("<subroutineBody>\n");
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '{') {
			System.err.println("Error compiling subroutine body!");
			return;
		}
		_bw.write("<symbol> " + '{'  + " </symbol>\n");

		handleMultipleVariableDeclarations();
		compileStatements();

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '}') {
			System.err.println("Error compiling subroutine body!");
			return;
		}
		_bw.write("<symbol> " + '}'  + " </symbol>\n");

		_bw.write("</subroutineBody>\n");
	}

	private void handleMultipleVariableDeclarations() throws IOException {
		boolean hasMoreVarDecs = false;
		do {
			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || !_tokenizer.keyWord().equals(KeyWord.VAR)) {
				_tokenizer.retreat();
				return;
			}
			_tokenizer.retreat();
			hasMoreVarDecs = true;
		
			compileVarDec();
		}
		while (hasMoreVarDecs);
	}

	public void compileParameterList() throws IOException {
		_bw.write("<parameterList>\n");
		
		// check for end of parameter list
		_tokenizer.advance();
		if (_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() == ')') {
			_tokenizer.retreat();
			_bw.write("</parameterList>\n");
			return;
		}

		// handle type
		if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.IDENTIFIER))) {
			System.err.println("Error compiling parameter list!");
			return;
		}
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
			if (!(_tokenizer.keyWord() == KeyWord.INT || _tokenizer.keyWord() == KeyWord.CHAR
					|| _tokenizer.keyWord() == KeyWord.BOOLEAN)) {

				System.err.println("Error compiling parameter list!");
				return;
			}
			if (_tokenizer.keyWord().equals(KeyWord.INT)) {
				_bw.write("<keyword> int </keyword>\n");
			}
			else if (_tokenizer.keyWord().equals(KeyWord.CHAR)) {
				_bw.write("<keyword> char </keyword>\n");
			}
			else {
				_bw.write("<keyword> boolean </keyword>\n");
			}
		}
		else {
			_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		}

		// handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling parameter list!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		handleMultipleParameters();

		_bw.write("</parameterList>\n");
	}
	
	private void handleMultipleParameters() throws IOException {
		boolean endParenthesisFound = false;
		while (!endParenthesisFound) {
			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || (_tokenizer.symbol() != ',' && _tokenizer.symbol() != ')')) {
				System.err.println("Error in handleMultipleVariableDeclarations!");
				return;
			}
	
			if (_tokenizer.symbol() == ',') {
				_bw.write("<symbol> " + ',' + " </symbol>\n");

				// handle type
				_tokenizer.advance();
				if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.IDENTIFIER))) {
					System.err.println("Error compiling multiple parameters in parameter list!");
					return;
				}
				if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
					if (!(_tokenizer.keyWord() == KeyWord.INT || _tokenizer.keyWord() == KeyWord.CHAR
							|| _tokenizer.keyWord() == KeyWord.BOOLEAN)) {

						System.err.println("Error compiling parameter list!");
						return;
					}
					if (_tokenizer.keyWord().equals(KeyWord.INT)) {
						_bw.write("<keyword> int </keyword>\n");
					}
					else if (_tokenizer.keyWord().equals(KeyWord.CHAR)) {
						_bw.write("<keyword> char </keyword>\n");
					}
					else {
						_bw.write("<keyword> boolean </keyword>\n");
					}
				}
				else {
					_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
				}

				// handle varName
				_tokenizer.advance();
				if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
					System.err.println("Error compiling multiple parameters in parameter list!");
					return;
				}
				_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
			}
			else {
				endParenthesisFound = true;
				_tokenizer.retreat();
			}
		}
	}

	public void compileVarDec() throws IOException {
		_bw.write("<varDec>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || !_tokenizer.keyWord().equals(KeyWord.VAR)) {
			System.err.println("Error in compileVarDec!");
			return;
		}
		_bw.write("<keyword> " + "var" + "</keyword>\n");

		// handle type
		_tokenizer.advance();
		if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.IDENTIFIER))) {
			System.err.println("Error in compileVarDec!");
			return;
		}
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
			if (!(_tokenizer.keyWord() == KeyWord.INT || _tokenizer.keyWord() == KeyWord.CHAR
					|| _tokenizer.keyWord() == KeyWord.BOOLEAN)) {

				System.err.println("Error in compileVarDec!");
				return;
			}
			if (_tokenizer.keyWord().equals(KeyWord.INT)) {
				_bw.write("<keyword> int </keyword>\n");
			}
			else if (_tokenizer.keyWord().equals(KeyWord.CHAR)) {
				_bw.write("<keyword> char </keyword>\n");
			}
			else {
				_bw.write("<keyword> boolean </keyword>\n");
			}
		}
		else {
			_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		}

		// Handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling class variables declaration!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		// Handle ',' or ';'
		handleVariableDeclarationList();

		_bw.write("</varDec>\n");
	}
	
	public void compileStatements() throws IOException {
		_bw.write("<statements>\n");
		
		while (true) {
			_tokenizer.advance();
			if (!(_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.tokenType().equals(TokenType.SYMBOL))) {
				System.err.println("Error compiling statements!");
				return;
			}

			if (_tokenizer.tokenType().equals(TokenType.KEYWORD)) {
				if (!(_tokenizer.keyWord().equals(KeyWord.LET) || _tokenizer.keyWord().equals(KeyWord.IF)
						|| _tokenizer.keyWord().equals(KeyWord.WHILE) || _tokenizer.keyWord().equals(KeyWord.DO)
						|| _tokenizer.keyWord().equals(KeyWord.RETURN))) {

					System.err.println("Error compiling statements!");
					return;
				}
				
				switch(_tokenizer.keyWord()) {
				case LET:
					_tokenizer.retreat();
					compileLet();
					break;
				case IF:
					_tokenizer.retreat();
					compileIf();
					break;
				case WHILE:
					_tokenizer.retreat();
					compileWhile();
					break;
				case DO:
					_tokenizer.retreat();
					compileDo();
					break;
				case RETURN:
					_tokenizer.retreat();
					compileReturn();
					break;
				default:
					System.err.println("Error compiling statements!");
					return;
				}
			}
			else {
				_tokenizer.retreat();
				break;
			}
		}
		_bw.write("</statements>\n");
	}
	
	public void compileDo() throws IOException {
		_bw.write("<doStatement>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.keyWord() != KeyWord.DO) {
			System.err.println("Error compiling Do!");
			return;
		}
		_bw.write("<keyword> do </keyword>\n");
		
		compileSubroutineCall();
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ';') {
			System.err.println("Error compiling Do!");
			return;
		}
		_bw.write("<symbol> " + ';' + "</symbol>\n");

		_bw.write("</doStatement>\n");
	}
	
	private void compileSubroutineCall() throws IOException {
		_tokenizer.advance();
		if (_tokenizer.tokenType() != TokenType.IDENTIFIER) {
			System.err.println("Error compiling subroutine call!");
		}
		String varName = _tokenizer.identifier();

		_tokenizer.advance();
		if (!(_tokenizer.tokenType() == TokenType.SYMBOL && (_tokenizer.symbol() == '(' || _tokenizer.symbol() == '.'))) {
			System.err.println("Error compiling subroutine call!");
		}
		if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '(') {
			_bw.write("<identifier> " + varName + " </identifier>\n");
			_bw.write("<symbol> " + '(' + " </symbol>\n");
			
			compileExpressionList();

			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
				System.err.println("Error compiling subroutine call!");
				return;
			}
			_bw.write("<symbol> " + ')' + " </symbol>\n");
		}
		else {
			_bw.write("<identifier> " + varName + " </identifier>\n");
			_bw.write("<symbol> " + '.' + " </symbol>\n");
			
			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
				System.err.println("Error compiling subroutine call!");
				return;
			}
			_bw.write("<identifier> " + _tokenizer.identifier() + " </identifier>\n");

			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() != '(') {
				System.err.println("Error compiling subroutine call!");
				return;
			}
			_bw.write("<symbol> " + '(' + " </symbol>\n");
			
			compileExpressionList();

			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
				System.err.println("Error compiling subroutine call!");
				return;
			}
			_bw.write("<symbol> " + ')' + " </symbol>\n");
		}
	}

	public void compileLet() throws IOException {
		_bw.write("<letStatement>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.keyWord() != KeyWord.LET) {
			System.err.println("Error compiling Let!");
			return;
		}
		_bw.write("<keyword> let </keyword>\n");

		// handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling Let!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		handleOptionalExpressionInSquareBrackets();

		// handle =
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '=') {
			System.err.println("Error compiling Let!");
			return;
		}
		_bw.write("<symbol> " + '=' + " </symbol>\n");
		
		compileExpression();

		// handle ;
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ';') {
			System.err.println("Error compiling Let!");
			return;
		}
		_bw.write("<symbol> " + ';' + " </symbol>\n");
		
		_bw.write("</letStatement>\n");
	}
	
	private void handleOptionalExpressionInSquareBrackets() throws IOException {
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '[') {
			_tokenizer.retreat();
			return;
		}
		_bw.write("<symbol> " + '[' + " </symbol>\n");
		
		compileExpression();

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ']') {
			System.err.println("Error handling option expression in square brackets!");
			return;
		}
		_bw.write("<symbol> " + ']' + " </symbol>\n");
	}

	public void compileWhile() throws IOException {
		_bw.write("<whileStatement>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.keyWord() != KeyWord.WHILE) {
			System.err.println("Error compiling While!");
			return;
		}
		_bw.write("<keyword> while </keyword>\n");
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '(') {
			System.err.println("Error compiling While!");
			return;
		}
		_bw.write("<symbol> " + '(' + " </symbol>\n");
		
		compileExpression();
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
			System.err.println("Error compiling While!");
			return;
		}
		_bw.write("<symbol> " + ')' + " </symbol>\n");
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '{') {
			System.err.println("Error compiling While!");
			return;
		}
		_bw.write("<symbol> " + '{' + " </symbol>\n");

		compileStatements();

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '}') {
			System.err.println("Error compiling While!");
			return;
		}
		_bw.write("<symbol> " + '}' + " </symbol>\n");

		_bw.write("</whileStatement>\n");
	}
	
	public void compileReturn() throws IOException {
		_bw.write("<returnStatement>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.keyWord() != KeyWord.RETURN) {
			System.err.println("Error compiling Return!");
			return;
		}
		_bw.write("<keyword> return </keyword>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ';') {
			_tokenizer.retreat();
			
			compileExpression();
			
			_tokenizer.advance();
		}
		_bw.write("<symbol> ; </symbol>\n");

		_bw.write("</returnStatement>\n");
	}
	
	public void compileIf() throws IOException {
		_bw.write("<ifStatement>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || _tokenizer.keyWord() != KeyWord.IF) {
			System.err.println("Error compiling If!");
			return;
		}
		_bw.write("<keyword> if </keyword>\n");

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '(') {
			System.err.println("Error compiling If!");
			return;
		}
		_bw.write("<symbol> " + '(' + " </symbol>\n");
		
		compileExpression();

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
			System.err.println("Error compiling If!");
			return;
		}
		_bw.write("<symbol> " + ')' + " </symbol>\n");
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '{') {
			System.err.println("Error compiling If!");
			return;
		}
		_bw.write("<symbol> " + '{' + " </symbol>\n");
		
		compileStatements();
		
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '}') {
			System.err.println("Error compiling If!");
			return;
		}
		_bw.write("<symbol> " + '}' + " </symbol>\n");
		
		_tokenizer.advance();
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD) && _tokenizer.keyWord() == KeyWord.ELSE) {
			_bw.write("<keyword> else </keyword>\n");

			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '{') {
				System.err.println("Error compiling Else!");
				return;
			}
			_bw.write("<symbol> " + '{' + " </symbol>\n");
			
			compileStatements();

			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != '}') {
				System.err.println("Error compiling Else!");
				return;
			}
			_bw.write("<symbol> " + '}' + " </symbol>\n");
		}
		else {
			_tokenizer.retreat();
		}
		
		_bw.write("</ifStatement>\n");
	}
	
	public void compileExpression() throws IOException {
		_bw.write("<expression>\n");
		
		compileTerm();
		
		_tokenizer.advance();
		if (_tokenizer.tokenType() == TokenType.SYMBOL
				&& (_tokenizer.symbol() == '+' || _tokenizer.symbol() == '-' || _tokenizer.symbol() == '*'
				|| _tokenizer.symbol() == '/' || _tokenizer.symbol() == '&' || _tokenizer.symbol() == '|'
				|| _tokenizer.symbol() == '<' || _tokenizer.symbol() == '>' || _tokenizer.symbol() == '=')) {

			_tokenizer.retreat();
			compileOpTerm();
		}
		else {
			_tokenizer.retreat();
		}

		_bw.write("</expression>\n");
	}
	
	private void compileOpTerm() throws IOException {
		do {
			_tokenizer.advance();
			if (!(_tokenizer.tokenType() == TokenType.SYMBOL
					&& (_tokenizer.symbol() == '+' || _tokenizer.symbol() == '-' || _tokenizer.symbol() == '*'
					|| _tokenizer.symbol() == '/' || _tokenizer.symbol() == '&' || _tokenizer.symbol() == '|'
					|| _tokenizer.symbol() == '<' || _tokenizer.symbol() == '>' || _tokenizer.symbol() == '='))) {
				
				System.err.println("Error compiling op term!");
				return;
			}
			String symbolString = getEscapedSymbol(_tokenizer.symbol());
			_bw.write("<symbol> " + symbolString + " </symbol>\n");
			
			compileTerm();
			
			_tokenizer.advance();
			if (!(_tokenizer.tokenType() == TokenType.SYMBOL
					&& (_tokenizer.symbol() == '+' || _tokenizer.symbol() == '-' || _tokenizer.symbol() == '*'
					|| _tokenizer.symbol() == '/' || _tokenizer.symbol() == '&' || _tokenizer.symbol() == '|'
					|| _tokenizer.symbol() == '<' || _tokenizer.symbol() == '>' || _tokenizer.symbol() == '='))) {
				
				_tokenizer.retreat();
				return;
			}
		}
		while (true);
	}

	public void compileTerm() throws IOException {
		_bw.write("<term>\n");
		
		_tokenizer.advance();
		if (_tokenizer.tokenType() == TokenType.INT_CONST) {
			_bw.write("<integerConstant> " + _tokenizer.intVal() + " </integerConstant>\n");
		}
		else if (_tokenizer.tokenType() == TokenType.STRING_CONST) {
			_bw.write("<stringConstant> " + _tokenizer.stringVal() + " </stringConstant>\n");
		}
		else if (_tokenizer.tokenType() == TokenType.KEYWORD
				&& (_tokenizer.keyWord() == KeyWord.TRUE || _tokenizer.keyWord() == KeyWord.FALSE
				|| _tokenizer.keyWord() == KeyWord.NULL || _tokenizer.keyWord() == KeyWord.THIS)) {

			_bw.write("<keyword> " + _tokenizer.keyWord().toString().toLowerCase() + " </keyword>\n");
		}
		else if (_tokenizer.tokenType() == TokenType.IDENTIFIER) {
			String varName = _tokenizer.identifier();

			_tokenizer.advance();
			if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '[') {
				_bw.write("<identifier> " + varName + " </identifier>\n");
				_bw.write("<symbol> " + '[' + " </symbol>\n");
				
				compileExpression();

				_tokenizer.advance();
				if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ']') {
					System.err.println("Error compiling array access!");
					return;
				}
				_bw.write("<symbol> " + ']' + " </symbol>\n");
			}
			else if (_tokenizer.tokenType() == TokenType.SYMBOL && (_tokenizer.symbol() == '(' || _tokenizer.symbol() == '.')) {
				if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '(') {
					_bw.write("<identifier> " + varName + " </identifier>\n");
					_bw.write("<symbol> " + '(' + " </symbol>\n");
					
					compileExpressionList();

					_tokenizer.advance();
					if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
						System.err.println("Error compiling subroutine call!");
						return;
					}
					_bw.write("<symbol> " + ')' + " </symbol>\n");
				}
				else if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '.') {
					_bw.write("<identifier> " + varName + " </identifier>\n");
					_bw.write("<symbol> " + '.' + " </symbol>\n");
					
					_tokenizer.advance();
					if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
						System.err.println("Error compiling subroutine call!");
						return;
					}
					_bw.write("<identifier> " + _tokenizer.identifier() + " </identifier>\n");

					_tokenizer.advance();
					if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() != '(') {
						System.err.println("Error compiling subroutine call!");
						return;
					}
					_bw.write("<symbol> " + '(' + " </symbol>\n");
					
					compileExpressionList();

					_tokenizer.advance();
					if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != ')') {
						System.err.println("Error compiling subroutine call!");
						return;
					}
					_bw.write("<symbol> " + ')' + " </symbol>\n");
				}
				else {
					System.err.println("Error compiling subroutine call!");
				}
			}
			else {
				_bw.write("<identifier> " + varName + " </identifier>\n");
				_tokenizer.retreat();
			}
		}
		else if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '(') {
			_bw.write("<symbol> " + '(' + " </symbol>\n");
			
			compileExpression();

			_tokenizer.advance();
			if (!(_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == ')')) {
				System.err.println("Error compiling term of the form '('expression')'");
			}
			_bw.write("<symbol> " + ')' + " </symbol>\n");
		}
		else if (_tokenizer.tokenType() == TokenType.SYMBOL
				&& (_tokenizer.symbol() == '-' || _tokenizer.symbol() == '~')) {

			_bw.write("<symbol> " + _tokenizer.symbol() + " </symbol>\n");
			
			compileTerm();
		}
		else {
			System.err.println("Error compiling term!");
		}

		_bw.write("</term>\n");
	}
	
	public void compileExpressionList() throws IOException {
		_bw.write("<expressionList>\n");

		_tokenizer.advance();
		if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == ')') {
			_tokenizer.retreat();

			_bw.write("</expressionList>\n");

			return;
		}
		
		_tokenizer.retreat();
		
		compileExpression();

		do {
			_tokenizer.advance();
			if (_tokenizer.tokenType().equals(TokenType.SYMBOL) && _tokenizer.symbol() == ',') {
				_bw.write("<symbol> " + ',' + "</symbol>\n");
				
				compileExpression();
			}
			else {
				_tokenizer.retreat();

				_bw.write("</expressionList>\n");

				return;
			}
		}
		while (true);
	}
	
	// to invoke this method, next token must a comma or a semicolon symbol
	private void handleVariableDeclarationList() throws IOException {
		boolean semiColonFound = false;
		while (!semiColonFound) {
			_tokenizer.advance();
			if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || (_tokenizer.symbol() != ',' && _tokenizer.symbol() != ';')) {
				System.err.println("Error in handleMultipleVariableDeclarations!");
				return;
			}
			_bw.write("<symbol> " + _tokenizer.symbol() + " </symbol>\n");
	
			if (_tokenizer.symbol() == ',') {
				_tokenizer.advance();
				if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
					System.err.println("Error in handleMultipleVariableDeclarations!");
					return;
				}
				_bw.write("<identifier> " + _tokenizer.identifier() + " </identifier>\n");
			}
			else {
				semiColonFound = true;
			}
		}
	}

	private BufferedWriter _bw;
	private JackTokenizer _tokenizer;
	
}
