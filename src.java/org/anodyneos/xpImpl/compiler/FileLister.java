package org.anodyneos.xpImpl.compiler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Stack;

public class FileLister {

    public static File[] deepListFiles(File root, FileFilter filter) {
        if(! root.isDirectory()) {
            return new File[0];
        } else {
            ArrayList results = new ArrayList();
            Stack dirs = new Stack();
            dirs.push(root);
            while (! dirs.empty()) {
                File[] files;
                files = ((File)dirs.pop()).listFiles(filter);
                for(int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        dirs.push(file);
                    } else {
                        results.add(file);
                    }
                }
            }
            return (File[]) results.toArray(new File[results.size()]);
        }
    }

    public static String[] deepList(File root, FileFilter filter) {
        File[] files = deepListFiles(root, filter);
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }
        return paths;
    }

}
