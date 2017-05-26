package org.jrao.jacksyntaxanalyzer;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	public SymbolTable() {
		_classSymbolTable = new HashMap<String, SymbolTableRow>();
		_subroutineSymbolTable = new HashMap<String, SymbolTableRow>();
		_nextStaticNumber = 0;
		_nextFieldNumber = 0;
		_nextArgNumber = 0;
		_nextVarNumber = 0;
	}

	/*
	 * Starts a new subroutine scope (i.e., resets the subroutine's symbol
	 * table)
	 */
	public void startSubroutine() {
		_subroutineSymbolTable.clear();
	}

	/*
	 * Defines a new identifier of the given name, type, and kind and assigns it
	 * a running index. STATIC and FIELD identifiers have a class scope, while
	 * ARG and VAR identifiers have a subroutine scope.
	 */
	public void define(String name, String type, Kind kind) {
		SymbolTableRow row = new SymbolTableRow(name, type, kind, varCount(kind));
		if (kind == Kind.STATIC) {
			_classSymbolTable.put(name, row);
			_nextStaticNumber++;
		}
		else if (kind == Kind.FIELD) {
			_classSymbolTable.put(name, row);
			_nextFieldNumber++;
		}
		else if (kind == Kind.ARG) {
			_subroutineSymbolTable.put(name, row);
			_nextArgNumber++;
		}
		else if (kind == Kind.VAR) {
			_subroutineSymbolTable.put(name, row);
			_nextVarNumber++;
		}
	}
	
	/*
	 * Returns the number of variables of the given kind already defined in the current scope
	 */
	public int varCount(Kind kind) {
		if (kind == Kind.STATIC) {
			return _nextStaticNumber;
		}
		else if (kind == Kind.FIELD) {
			return _nextFieldNumber;
		}
		else if (kind == Kind.ARG) {
			return _nextArgNumber;
		}
		else if (kind == Kind.VAR) {
			return _nextVarNumber;
		}
		else {
			return -1;
		}
	}
	
	/*
	 * Returns the kind of the named identifier in the current scope. If the
	 * identifier is unknown in the current scope, return NONE.
	 */
	public Kind kindOf(String name) {
		if (_subroutineSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _subroutineSymbolTable.get(name);
			return row.getKind();
		}
		else if (_classSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _classSymbolTable.get(name);
			return row.getKind();
		}
		return Kind.NONE;
	}
	
	/*
	 * Returns the type of the named identifier in the current scope
	 */
	public String typeOf(String name) {
		if (_subroutineSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _subroutineSymbolTable.get(name);
			return row.getType();
		}
		else if (_classSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _classSymbolTable.get(name);
			return row.getType();
		}
		return "";
	}
	
	/*
	 * Returns the index assigned to the named identifier
	 */
	public int indexOf(String name) {
		if (_subroutineSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _subroutineSymbolTable.get(name);
			return row.getNumber();
		}
		else if (_classSymbolTable.containsKey(name)) {
			SymbolTableRow row = (SymbolTableRow) _classSymbolTable.get(name);
			return row.getNumber();
		}
		return -1;
	}
	
	public void incrementStaticNumber() {
		_nextStaticNumber++;
	}
	
	public void incrementFieldNumber() {
		_nextFieldNumber++;
	}
	
	public void incrementArgNumber() {
		_nextArgNumber++;
	}

	public void incrementVarNumber() {
		_nextVarNumber++;
	}

	public void resetStaticNumber() {
		_nextStaticNumber = 0;
	}
	
	public void resetFieldNumber() {
		_nextFieldNumber = 0;
	}

	public void resetArgNumber() {
		_nextArgNumber = 0;
	}

	public void resetVarNumber() {
		_nextVarNumber = 0;
	}
	
	private Map _classSymbolTable;
	private Map _subroutineSymbolTable;
	private int _nextStaticNumber;
	private int _nextFieldNumber;
	private int _nextArgNumber;
	private int _nextVarNumber;

}
