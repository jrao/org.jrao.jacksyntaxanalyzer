package org.jrao.jacksyntaxanalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzerTokenizerOnly {

	public static void main(String[] args) {
        System.out.println("Welcome to org.jrao.jacksyntaxanalyzer!\n");
        
        if (args.length < 1) {
        	System.err.println("Usage: java -jar org.jrao.jacksyntaxanalyzer.jar [path-to-jack-source-file-or-directory]");
        	return;
        }
        
		String currentPathString = Paths.get("").toAbsolutePath().toString();
		String inputPathString = currentPathString + "/" + args[0];
        File inputFile = new File(inputPathString);
        
        List<File> jackFiles = new ArrayList<File>();
        if (inputFile.isFile()) {
        	if (inputPathString.endsWith(".jack")) {
        		jackFiles.add(inputFile);
        	}
        }
        else {
        	File[] jackFileArray = inputFile.listFiles(new FilenameFilter() {
        		@Override
        		public boolean accept(File dir, String name) {
        			return name.toLowerCase().endsWith(".jack");
        		}
        	});
        	if (jackFileArray != null) {
				for (File jackFile : jackFileArray) {
					jackFiles.add(jackFile);
				}
        	}
        }
        
        for (File jackFile : jackFiles) {
			JackTokenizer tokenizer = null;
			try {
				tokenizer = new JackTokenizer(jackFile);
			}
			catch (IOException ioe) {
				System.err.println("Failed to create tokenizer!");
				
				ioe.printStackTrace();
				
				return;
			}
			
			try {
				tokenizer.writeXML();
				System.out.println("Wrote XML!");
			}
			catch (IOException ieo) {
				System.err.println("Error writing XML!");
			}
			
			try {
				tokenizer.close();
			}
			catch (IOException ioe) {
				System.err.println("Failed to close writer!");
			}
        }
	}

}
