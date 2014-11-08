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

package co.fizzed.stork.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class AssemblyUtils {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyUtils.class);
    
    static public void copyStandardProjectResources(File projectDir, File outputDir) throws IOException {
        FileUtils.copyDirectory(projectDir, outputDir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName().toLowerCase();
                if (name.startsWith("readme") || name.startsWith("changelog") || name.startsWith("release") || name.startsWith("license")) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
    
    /**
     * Create .tar.gz archive file "name.tar.gz" with the contents of inputDir
     * using the prefix of "/name"
     * @param outputDir
     * @param inputDir
     * @param name
     * @return
     * @throws IOException 
     */
    static public File createTGZ(File outputDir, File inputDir, String name) throws IOException {
        // create tarball
        File tgzFile = new File(outputDir, name + ".tar.gz");
        TarArchiveOutputStream tgzout = null;
        try {
            tgzout = AssemblyUtils.createTGZStream(tgzFile);
            addFileToTGZStream(tgzout, inputDir, name, false);
        } finally {
            if (tgzout != null) {
                    tgzout.close();
            }
        }
        return tgzFile;
    }
    
    static public TarArchiveOutputStream createTGZStream(File tgzFile) throws IOException {
        TarArchiveOutputStream tgzout = new TarArchiveOutputStream(
              new GZIPOutputStream(
                   new BufferedOutputStream(new FileOutputStream(tgzFile))));
        tgzout.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tgzout.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        return tgzout;
    }
    
    static public void addFileToTGZStream(TarArchiveOutputStream tgzout, File f, String base, boolean appendName) throws IOException {
        //File f = new File(path);
        String entryName = base;
        if (appendName) {
            if (!entryName.equals("")) {
                if (!entryName.endsWith("/")) {
                    entryName += "/" + f.getName();
                } else {
                    entryName += f.getName();
                }
            } else {
                entryName += f.getName();
            }
        }
        TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);

        if (f.isFile()) {
            if (f.canExecute()) {
                // -rwxr-xr-x
                tarEntry.setMode(493);
            } else {
                // keep default mode
            }
        }

        tgzout.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            FileInputStream in = new FileInputStream(f);
            IOUtils.copy(in, tgzout);
            in.close();
            tgzout.closeArchiveEntry();
            
        } else {
            tgzout.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null){
                for (File child : children) {
                    logger.info(" adding: " + entryName + "/" + child.getName());
                    addFileToTGZStream(tgzout, child, entryName + "/", true);
                }
            }
        }
    }
    
}
