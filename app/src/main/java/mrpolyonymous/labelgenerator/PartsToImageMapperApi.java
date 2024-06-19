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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mrpolyonymous.labelgenerator.MyParts.PartDetails;


public class PartsToImageMapperApi {

    // TODO better name
    
    /** Parts categories that will always be ignored */
    static final Set<String> IGNORE_CATEGORIES;
    
    static {
        
        IGNORE_CATEGORIES = new HashSet<>();
        IGNORE_CATEGORIES.add("17"); // Non-LEGO
//        IGNORE_CATEGORIES.add("41"); // Bionicle
        IGNORE_CATEGORIES.add("58"); // Stickers
        IGNORE_CATEGORIES.add("59"); // Minifig Heads
        IGNORE_CATEGORIES.add("60"); // Minifig Upper Body
        IGNORE_CATEGORIES.add("61"); // Minifig Lower Body
        IGNORE_CATEGORIES.add("62"); // Minidoll Heads
        IGNORE_CATEGORIES.add("63"); // Minidoll Upper Body
        IGNORE_CATEGORIES.add("64"); // Minidoll Lower Body
        
    }

    

    private final File dataFolder;

    public PartsToImageMapperApi(File dataFolder) {
        try {
            this.dataFolder = dataFolder.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Could not resolve path " + dataFolder, e);
        }

        if (!this.dataFolder.exists() || !this.dataFolder.isDirectory()) {
            throw new IllegalArgumentException("Invalid data directory " + dataFolder);
        }

    }

    public PartsToImages mapPartsToImages(MyParts allMyParts, LocalPartsDatabase localPartsDatabase) throws IOException {

        System.out.println("Number of part/colour combinations: " + allMyParts.size());
        
        Map<PartAndQuantitiesByColour, ImageInfo> partToImage = new HashMap<>();
        List<PartAndQuantitiesByColour> uniqueParts = new ArrayList<>();
        List<PartAndQuantitiesByColour> missingImageParts = new ArrayList<>();
        for (Map.Entry<String, List<PartDetails>> entry : allMyParts.getPartsMap().entrySet()) {
            Part part = entry.getValue().get(0).part();
            if (IGNORE_CATEGORIES.contains(part.partCategoryId())) {
                continue;
            }
            
            if (!part.id().equals(part.idIgnoringPrint())) {
                // ignore prints if the base part type is also in the database
                Part partWithoutPrint = allMyParts.getPartForId(part.idIgnoringPrint());
                if (partWithoutPrint != null && partWithoutPrint.id().equals(partWithoutPrint.idIgnoringPrint())) {
                    continue;
                }
                // TODO - if all variants of a part are prints but the base part isn't in the
                // inventory, substitute the base part with no print instead
            }
            File imageFile = localPartsDatabase.getImageForPart(part.id());
            if (imageFile == null) {
                System.out.println("No image for part " + part);
                missingImageParts.add(new PartAndQuantitiesByColour(part));
            } else {
                partToImage.put(new PartAndQuantitiesByColour(part), new ImageInfo(imageFile, 50, 50));
            }
        }

        return new PartsToImages(partToImage, uniqueParts, missingImageParts);
    }
    
}
