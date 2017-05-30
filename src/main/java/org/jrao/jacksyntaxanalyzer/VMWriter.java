package org.jrao.jacksyntaxanalyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
	
	/*
	 * Create a new output.vm file and prepares it for writing
	 */
	public VMWriter(File outputFile) throws IOException {
		_bw = new BufferedWriter(new FileWriter(outputFile));
	}
	
	/*
	 * Writes a VM push command
	 */
	public void writePush(String segment, int index) throws IOException {
		switch (segment) {
			case "CONST":
			case "ARG":
			case "LOCAL":
			case "STATIC":
			case "THIS":
			case "THAT":
			case "POINTER":
			case "TEMP":
				_bw.write("push " + segment + " " + index);
				break;
			default:
				System.err.println("Error: Invalid argument 'segment': " + segment + "\n");
				break;
		}
	}

	/*
	 * Writes a VM pop command
	 */
	public void writePop(String segment, int index) throws IOException {
		switch (segment) {
			case "CONST":
			case "ARG":
			case "LOCAL":
			case "STATIC":
			case "THIS":
			case "THAT":
			case "POINTER":
			case "TEMP":
				_bw.write("pop " + segment + " " + index);
				break;
			default:
				System.err.println("Error: Invalid argument 'segment': " + segment + "\n");
				break;
		}
	}

	/*
	 * Writes a VM arithmetic-logical command
	 */
	public void writeArithmetic(String command) throws IOException {
		switch (command) {
			case "ADD":
			case "SUB":
			case "NEG":
			case "EQ":
			case "GT":
			case "LT":
			case "AND":
			case "OR":
			case "NOT":
				_bw.write(command + "\n");
				break;
			default:
				System.err.println("Error: Invalid argument 'command': " + command + "\n");
				break;
		}
	}

	/*
	 * Writes a VM label command
	 */
	public void writeLabel(String label) throws IOException {
		_bw.write("label " + label + "\n");
	}

	/*
	 * Writes a VM goto command
	 */
	public void writeGoto(String label) throws IOException {
		_bw.write("goto " + label + "\n");
	}

	/*
	 * Writes a VM if-goto command
	 */
	public void writeIf(String label) throws IOException {
		_bw.write("if-goto " + label + "\n");
	}

	/*
	 * Writes a VM call command
	 */
	public void writeCall(String name, int nArgs) throws IOException {
		_bw.write("call " + name + " " + nArgs + "\n");
	}

	/*
	 * Writes a VM function command
	 */
	public void writeFunction(String name, int nLocals) throws IOException {
		_bw.write("function " + name + " " + nLocals + "\n");
	}

	/*
	 * Writes a VM return command
	 */
	public void writeReturn() throws IOException {
		_bw.write("return \n");
	}
	
	/*
	 * Closes the output file
	 */
	public void close() throws IOException {
		if (_bw != null) {
			_bw.flush();
			_bw.close();
		}
	}
	
	private BufferedWriter _bw;

}
