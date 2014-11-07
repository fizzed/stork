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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class TarUtils {
    private static final Logger logger = LoggerFactory.getLogger(TarUtils.class);
    
    static public TarArchiveOutputStream createTGZStream(File tgzFile) throws IOException {
        TarArchiveOutputStream tgzout = new TarArchiveOutputStream(
              new GZIPOutputStream(
                   new BufferedOutputStream(new FileOutputStream(tgzFile))));
        tgzout.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tgzout.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        return tgzout;
    }
    
    static public void addFileToTGZStream(TarArchiveOutputStream tgzout, String path, String base, boolean appendName) throws IOException {
        File f = new File(path);
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
                    addFileToTGZStream(tgzout, child.getAbsolutePath(), entryName + "/", true);
                }
            }
        }
    }
    
}
