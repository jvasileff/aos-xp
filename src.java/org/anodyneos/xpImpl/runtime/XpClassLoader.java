package org.anodyneos.xpImpl.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.anodyneos.xpImpl.translater.TranslaterContext;

public class XpClassLoader extends ClassLoader {

    private String root;
    private ClassLoader parent;
    public XpClassLoader(ClassLoader parent){
        super(parent);
        this.parent = parent;
    }


    public String getRoot() {
        return root;
    }
    public void setRoot(String root) {
        this.root = root;
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        Class clazz = null;
        clazz = findLoadedClass(name);
        if (clazz != null){
            if (resolve){
                resolveClass(clazz);
            }
            return clazz;
        }

        if (name.startsWith(TranslaterContext.DEFAULT_PACKAGE + ".")){

            return findClass(name);

        }else{

            clazz = parent.loadClass(name);
            if (resolve){
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    public Class<?> findClass(String name) throws ClassNotFoundException{
        byte[] b = loadClassData(name);
        if (b == null){
            throw new ClassNotFoundException(getFileNameFromClassName(getRoot(),name));
        }else{
            return defineClass(name, b, 0, b.length);
        }
    }

    private byte[] loadClassData(String name) {

        try{
            File classFile = new File(getFileNameFromClassName(getRoot(),name));
            if (classFile.exists() && classFile.canRead()){
                return getBytesFromFile(classFile);
            }
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        return null;

    }

    public static String getFileNameFromClassName(String root, String className){
        if (!(root.endsWith("/") || root.endsWith("\\"))){
            root += File.separator;
        }
        String fileName = root + className.replace('.',File.separatorChar) + ".class";
        return fileName;
    }


    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
