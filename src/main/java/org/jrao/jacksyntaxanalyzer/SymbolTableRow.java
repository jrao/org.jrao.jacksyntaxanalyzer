package org.jrao.jacksyntaxanalyzer;

public class SymbolTableRow {
	
	public SymbolTableRow(String name, String type, Kind kind, int number) {
		_name = name;
		_type = type;
		_kind = kind;
		_number = number;
	}
	
	public String getName() {
		return _name;
	}

	public String getType() {
		return _type;
	}

	public Kind getKind() {
		return _kind;
	}

	public int getNumber() {
		return _number;
	}

	private String _name;
	private String _type;
	private Kind _kind;
	private int _number;

}
