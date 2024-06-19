/*
Copyright 2024 mrpolyonymous

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package mrpolyonymous.labelgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

public class PartsCsvDatabaseCreator {
    final File dataFolder;

    public PartsCsvDatabaseCreator(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public PartsCsvDatabase readRebrickablePartsData(Fetcher fetcher) throws IOException {
        fetchAndUnzipCsv(fetcher, "colors");
        fetchAndUnzipCsv(fetcher, "part_categories");
        fetchAndUnzipCsv(fetcher, "parts");
        fetchAndUnzipCsv(fetcher, "elements");
        
        PartsCsvDatabase partsDatabase = new PartsCsvDatabase();
        partsDatabase.readColours(new File(dataFolder, "colors.csv"));
        partsDatabase.readPartCategories(new File(dataFolder, "part_categories.csv"));
        partsDatabase.readFullPartsList(new File(dataFolder, "parts.csv"));
        partsDatabase.readElements(new File(dataFolder, "elements.csv"));
        return partsDatabase;
    }

    private void fetchAndUnzipCsv(Fetcher fetcher, String dataFile) throws IOException {

        String gzipFileName = dataFile + ".csv.gz";
        File gzipFile = new File(dataFolder, gzipFileName);
        String csvFileName = dataFile + ".csv";
        File csvFile = new File(dataFolder, csvFileName);
        
        if (csvFile.exists()) {
            System.out.println("CSV file " + csvFile + " exists, not downloading");
            return;
        }
        
        if (!gzipFile.exists()) {
            Future<File> gzipFileFuture = fetcher.fetchFromRebrickableCdnDownloadsAsync(gzipFileName, gzipFile);
            try {
                gzipFileFuture.get();
            } catch (InterruptedException e) {
                throw new RuntimeException("Download interrupted", e);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException ioEx) {
                    throw ioEx;
                }
                throw new IOException("Failed to fetch data file " + dataFile, e);
            }
        }
        
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile));
                FileOutputStream csvStream = new FileOutputStream(csvFile)) {
            gzis.transferTo(csvStream);
        }

        System.out.println("Extracted CSV file " + csvFile.getAbsolutePath());
    }

}
