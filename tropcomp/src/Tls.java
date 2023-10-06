import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Tls {
    private File dirPath;
    private List<List<String>> tlsValues;
    private List<String> dirToSkip;
    private Tloc tloc = new Tloc();
    private Tassert tassert = new Tassert();

    public Tls(String path) {
        this.dirPath = new File(path);
        this.tlsValues = new ArrayList<>();
        this.dirToSkip = new ArrayList<>();
        /*
        String filepath, packetname, classname;
        File directory = new File(path);
        try{
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".java")) {
                            filepath = file.getPath();
                            packetname = file.getClass().getPackageName();
                            classname = file.getClass().getName();
                        }
                    }
                }
            }
        } catch (Error e) {}
*/
    }

    // explore current directory level and look for test files
    // uses recursion to go into directory inside the current one
    public List<List<String>> exploreLevel() {
        File[] files = this.dirPath.listFiles();
        boolean containsSrc = true;
        boolean containsTest = true;
        boolean containsJava = true;
        System.out.println("given path: " + this.dirPath.getAbsolutePath());
        // path to an empty directory
        if(this.dirPath.getPath().matches(".*\\..*") && files == null) {

        }

        // not currently in "src" directory or lower
        if(!this.dirPath.getAbsolutePath().matches(".*\\Wsrc.*")) {
            // check if current directory contains "src" directory
            for(File f : files) {
                if(f.getName().compareTo("src") == 0) {
                    Tls tls = new Tls(f.getPath());
                    this.tlsValues = tls.exploreLevel();
                    return this.tlsValues;
                }
            }
            //System.out.println("no src dir");
            containsSrc = false;
        }
        // not currently in "test" repository or lower
        if(containsSrc && !this.dirPath.getAbsolutePath().matches(".*\\Wtest.*")) {
            // check if current directory contains "test" directory
            for(File f : files) {
                if(f.getName().compareTo("test") == 0) {
                    Tls tls = new Tls(f.getPath());
                    this.tlsValues = tls.exploreLevel();
                    return this.tlsValues;
                }
            }
            //System.out.println("no test dir");
            containsTest = false;
        }
        // not currently in "java" repository or lower
        if(containsSrc && containsTest && !this.dirPath.getAbsolutePath().matches(".*\\Wjava.*")) {
            // check if current directory contains "test" directory
            for(File f : files) {
                if(f.getName().compareTo("java") == 0) {
                    Tls tls = new Tls(f.getPath());
                    this.tlsValues = tls.exploreLevel();
                    return this.tlsValues;
                }
            }
            //System.out.println("no java dir");
            containsJava = false;
        }
        // directory does not contains java test file directory following Java & Maven format norms
        if(!containsJava) {System.out.println("no java file");return this.tlsValues;}

        for(File f : files) {
            String filePath = f.getPath(); // relative path of the file
            //System.out.println("file path: " + filePath);
            // file has no extension, it is a directory; explore it and add created entries to tlsValues
            if(!filePath.matches(".*\\..*")) {
                //System.out.println("no ext");
                Tls tls = new Tls(filePath);
                this.tlsValues.addAll(tls.exploreLevel());
                //System.out.println("no ext explore ret: " + this.tlsValues);
                return this.tlsValues;
            }
            // not a java file
            if(!filePath.endsWith(".java")) {
                System.out.println("not java file");continue;}
            // is a non-test java file
            if(!filePath.matches(".*\\W(Test)([A-Z]\\w*)+.java") &&
                    !filePath.matches(".*\\W([A-Z]\\w*)+(Test).java")) {
                System.out.println("not test file");continue;}

            List<String> tlsValuesEntry = new ArrayList<>();
            String absoluteFilePath, packageName = "", className;
            boolean packNameExtracted = false;
            int tlocValue, tassertValue, packNameStart;
            float tcmpValue;
            int fileExtensionIdx = filePath.indexOf(".java");
            // Unix system
            int lastIdxSeparator = filePath.lastIndexOf("/");
            // Windows system
            if(lastIdxSeparator < 0) lastIdxSeparator = filePath.lastIndexOf("\\");

            absoluteFilePath = f.getAbsolutePath();
            className = filePath.substring(lastIdxSeparator + 1, fileExtensionIdx);
            tlocValue = tloc.calculate(absoluteFilePath);
            tassertValue = tassert.calculate(absoluteFilePath);
            tcmpValue = (float)tlocValue / (float)tassertValue;
            // package name start after ".*/test/java/"
            packNameStart = absoluteFilePath.indexOf("java") + 5;
            lastIdxSeparator = absoluteFilePath.lastIndexOf("/");
            if(lastIdxSeparator < 0) lastIdxSeparator = absoluteFilePath.lastIndexOf("\\");
            if(packNameStart < lastIdxSeparator) // no package when this is false
                packageName = absoluteFilePath.substring(packNameStart, lastIdxSeparator);

            // add the values to the List<String> tlsValuesEntry and then to tlsValues
            tlsValuesEntry.add(absoluteFilePath);
            tlsValuesEntry.add(packageName);
            tlsValuesEntry.add(className);
            tlsValuesEntry.add(String.valueOf(tlocValue));
            tlsValuesEntry.add(String.valueOf(tassertValue));
            tlsValuesEntry.add(String.valueOf(tcmpValue));
            tlsValues.add(tlsValuesEntry);
        }

        return this.tlsValues;
    }



    @Override
    public String toString() {
        String output = ""; // initialize empty String
        for(List<String> lineContent : this.tlsValues) {
            for(int i = 0; i < lineContent.size(); i++) {
                output += lineContent.get(i);
                if(i != lineContent.size() - 1) output += ", ";
            }
            output += "\n";
        }

        return output;
    }


}
