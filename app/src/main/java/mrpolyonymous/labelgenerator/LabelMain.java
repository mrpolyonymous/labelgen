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
import java.io.IOException;

public class LabelMain {

    public static void main(String[] args) throws IOException {
        
        if (args.length < 2) {
            System.out.println("Usage: " + LabelMain.class.getName() + " <data_dir> <parts_file> [<grid_output_name>]");
            return;
        }
        
        File dataDir = new File(args[0]);
        if (dataDir.exists() && !dataDir.isDirectory()) {
            System.err.println(args[0] + " is not a valid directory");
            return;
        } else if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                System.err.println("Could not create directory " + dataDir);
                return;
            }
        }
        
        File partsFile = new File(args[1]);
        if (!partsFile.exists() || !partsFile.isFile()) {
            System.err.println("Could not find parts file " + partsFile);
            return;
        }
        
        File outputFile;
        if (args.length > 2) {
            outputFile = new File(args[2]);
        } else {
            String outputFileName = partsFile.getName();
            outputFileName = outputFileName.replace(".csv", "") + "-grid.html";
            outputFile = new File(partsFile.getParent(), outputFileName);
        }

        try (Fetcher fetcher = new Fetcher()) {

            PartsCsvDatabaseCreator dbCreator = new PartsCsvDatabaseCreator(dataDir);
            PartsCsvDatabase partsDatabase = dbCreator.readRebrickablePartsData(fetcher);

            MyParts myParts = new MyParts(partsDatabase);
            myParts.readMyParts(partsFile);

            PartsToImageMapper uniqueifier = new PartsToImageMapper(dataDir, partsDatabase, fetcher);

            PartsToImages results = uniqueifier.mapPartsToImages(myParts);

            OutputGenerator outputGenerator = new OutputGenerator(partsDatabase);
            outputGenerator.savePartsGrid(outputFile, results);
        }
    }

}
