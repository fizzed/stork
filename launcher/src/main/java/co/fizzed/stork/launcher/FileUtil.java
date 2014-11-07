/*
 * Copyright 2014 mfizz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.fizzed.stork.launcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 * @author joelauer
 */
public class FileUtil {
    
    static public List<File> findAllFiles(List<String> fileStrings) throws IOException {
        List<File> allFiles = new ArrayList<File>();
        for (String fileString : fileStrings) {
            List<File> files = FileUtil.findFiles(fileString);
            allFiles.addAll(files);
        }
        return allFiles;
    }
    
    static public List<File> findFiles(String fileString) throws IOException {
        if (fileString.endsWith("*")) {
            // file string contains a glob...
            File f = new File(fileString);
            File parent = f.getParentFile();
            if (parent == null) {
                parent = new File(".");
            }
            FileFilter fileFilter = new WildcardFileFilter(f.getName());
            File[] files = parent.listFiles(fileFilter);
            return Arrays.asList(files);
        } else {
            File f = new File(fileString);
            if (!f.exists()) {
                throw new IOException("File [" + fileString + "] does not exist");
            } else {
                if (f.isDirectory()) {
                    return Arrays.asList(f.listFiles());
                } else {
                    return Arrays.asList(f);
                }
            }
        }
    }
    
}
