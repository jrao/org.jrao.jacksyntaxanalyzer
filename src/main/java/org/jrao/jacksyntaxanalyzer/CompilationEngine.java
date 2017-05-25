package org.jrao.jacksyntaxanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
	
	public int nextStaticNumber;
	public int nextFieldNumber;
	public int nextArgNumber;
	public int nextVarNumber;
	
	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		_bw = new BufferedWriter(new FileWriter(outputFile));
		_tokenizer = new JackTokenizer(inputFile);
		
		nextStaticNumber = 0;
		nextFieldNumber = 0;
		nextArgNumber = 0;
		nextVarNumber = 0;
	}
	
	public void close() throws IOException {
		if (_bw != null) {
			_bw.flush();
			_bw.close();
		}
	}
	
	public void compileClass() throws IOException {
		_bw.write("<class>\n");

		eatKeyword(KeyWord.CLASS);

		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling class!");
			return;
		}
		_bw.write("<identifier category=\"class\" definition=\"true\"> " + _tokenizer.identifier() + " </identifier>\n");
		
		eatSymbol('{');
		
		compileClassVarDec();
		compileSubroutine();
		
		eatSymbol('}');
		
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
		Kind kind = Kind.NONE;
		if (_tokenizer.keyWord().equals(KeyWord.STATIC)) {
			_bw.write("<keyword> static </keyword>\n");
			kind = Kind.STATIC;
		}
		else {
			_bw.write("<keyword> field </keyword>\n");
			kind = Kind.FIELD;
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
			_bw.write("<identifier category=\"class\" definition=\"false\"> " + _tokenizer.identifier()  + " </identifier>\n");
		}

		// Handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling class variables declaration!");
			return;
		}
		if (kind == Kind.STATIC) {
			_bw.write("<identifier category=\"static\" number=\"" + nextStaticNumber + "\" definition=\"true\"> " + _tokenizer.identifier()  + " </identifier>\n");
			nextStaticNumber++;
		}
		else if (kind == Kind.FIELD) {
			_bw.write("<identifier category=\"field\" number=\"" + nextFieldNumber + "\" definition=\"true\"> " + _tokenizer.identifier()  + " </identifier>\n");
			nextFieldNumber++;
		}
		else {
			throw new IllegalStateException();
		}
		
		// Handle ',' or ';'
		handleVariableDeclarationList(kind);

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
		eatSymbol('(');
		
		compileParameterList();

		eatSymbol(')');
		
		compileSubroutineBody();
		
		_bw.write("</subroutineDec>\n");
		
		compileSubroutine();
	}
	
	private void compileSubroutineBody() throws IOException {
		_bw.write("<subroutineBody>\n");
		
		eatSymbol('{');

		handleMultipleVariableDeclarations();
		compileStatements();

		eatSymbol('}');

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

		eatKeyword(KeyWord.DO);
		
		compileSubroutineCall();
		
		eatSymbol(';');

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

			eatSymbol(')');
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

			eatSymbol('(');
			
			compileExpressionList();

			eatSymbol(')');
		}
	}

	public void compileLet() throws IOException {
		_bw.write("<letStatement>\n");

		eatKeyword(KeyWord.LET);

		// handle varName
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.IDENTIFIER)) {
			System.err.println("Error compiling Let!");
			return;
		}
		_bw.write("<identifier> " + _tokenizer.identifier()  + " </identifier>\n");
		
		handleOptionalExpressionInSquareBrackets();

		// handle =
		eatSymbol('=');
		
		compileExpression();

		// handle ;
		eatSymbol(';');
		
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

		eatSymbol(']');
	}

	public void compileWhile() throws IOException {
		_bw.write("<whileStatement>\n");

		eatKeyword(KeyWord.WHILE);
		
		eatSymbol('(');
		
		compileExpression();
		
		eatSymbol(')');
		
		eatSymbol('{');

		compileStatements();

		eatSymbol('}');

		_bw.write("</whileStatement>\n");
	}
	
	public void compileReturn() throws IOException {
		_bw.write("<returnStatement>\n");

		eatKeyword(KeyWord.RETURN);

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

		eatKeyword(KeyWord.IF);

		eatSymbol('(');
		
		compileExpression();

		eatSymbol(')');
		
		eatSymbol('{');
		
		compileStatements();
		
		eatSymbol('}');
		
		_tokenizer.advance();
		if (_tokenizer.tokenType().equals(TokenType.KEYWORD) && _tokenizer.keyWord() == KeyWord.ELSE) {
			_bw.write("<keyword> else </keyword>\n");

			eatSymbol('{');
			
			compileStatements();

			eatSymbol('}');
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

				eatSymbol(']');
			}
			else if (_tokenizer.tokenType() == TokenType.SYMBOL && (_tokenizer.symbol() == '(' || _tokenizer.symbol() == '.')) {
				if (_tokenizer.tokenType() == TokenType.SYMBOL && _tokenizer.symbol() == '(') {
					_bw.write("<identifier> " + varName + " </identifier>\n");
					_bw.write("<symbol> " + '(' + " </symbol>\n");
					
					compileExpressionList();

					eatSymbol(')');
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

					eatSymbol('(');
					
					compileExpressionList();

					eatSymbol(')');
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

			eatSymbol(')');
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
	private void handleVariableDeclarationList(Kind kind) throws IOException {
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
				if (kind == Kind.STATIC) {
					_bw.write("<identifier category=\"static\" number=\"" + nextStaticNumber + "\" definition=\"true\"> " + _tokenizer.identifier() + " </identifier>\n");
					nextStaticNumber++;
				}
				else if (kind == Kind.FIELD) {
					_bw.write("<identifier category=\"field\" number=\"" + nextFieldNumber + "\" definition=\"true\"> " + _tokenizer.identifier() + " </identifier>\n");
					nextStaticNumber++;
				}
				else if (kind == Kind.VAR) {
					_bw.write("<identifier category=\"var\" number=\"" + nextVarNumber + "\" definition=\"true\"> " + _tokenizer.identifier() + " </identifier>\n");
					nextVarNumber++;
				}
				else if (kind == Kind.ARG) {
					_bw.write("<identifier category=\"arg\" number=\"" + nextArgNumber + "\" definition=\"true\"> " + _tokenizer.identifier() + " </identifier>\n");
					nextArgNumber++;
				}
				else {
					throw new IllegalArgumentException();
				}
			}
			else {
				semiColonFound = true;
			}
		}
	}
	
	private String getEscapedSymbol(char symbol) {
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

	private void eatKeyword(KeyWord keyword) throws IOException {
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.KEYWORD) || !_tokenizer.keyWord().equals(keyword)) {
			System.err.println("Error eating keyword " + keyword + "!");
			return;
		}
		_bw.write("<keyword> " + keyword.toString().toLowerCase() + " </keyword>\n");
	}

	private void eatSymbol(char symbol) throws IOException {
		_tokenizer.advance();
		if (!_tokenizer.tokenType().equals(TokenType.SYMBOL) || _tokenizer.symbol() != symbol) {
			System.err.println("Error eating symbol " + symbol + "!");
			return;
		}
		_bw.write("<symbol> " + symbol + " </symbol>\n");
	}

	private BufferedWriter _bw;
	private JackTokenizer _tokenizer;
	
}
